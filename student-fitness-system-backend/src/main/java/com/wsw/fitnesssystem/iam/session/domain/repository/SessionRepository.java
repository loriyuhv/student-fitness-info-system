package com.wsw.fitnesssystem.iam.session.domain.repository;

import java.util.Optional;
import java.util.Set;

/**
 * 登录会话仓储（领域层定义的仓储接口）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>管理用户登录会话的生命周期：保存、查询、删除</li>
 *   <li>维护 AccessToken / RefreshToken 的映射关系</li>
 *   <li>维护令牌版本号，用于批量失效</li>
 *   <li>维护 AccessToken 黑名单，用于单条令牌失效</li>
 * </ul>
 *
 * <p><b>边界：</b>
 * <ul>
 *   <li>本接口只描述"需要什么能力"，不描述"如何存储"</li>
 *   <li>不出现 Redis key、Lua 脚本、TTL、ZSet、Hash 等技术细节</li>
 *   <li>具体实现由基础设施层提供（当前为 {@code RedisSessionRepository}）</li>
 * </ul>
 *
 * <p><b>批量失效语义：</b>
 * {@link #removeAllSessions(long, long)} 采用"令牌版本号自增"方式实现批量失效，
 * 不保证对所有已签发 AccessToken 的即时黑名单化，具体取舍见实现层文档。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/3/21 10:05
 * @since 1.0
 */
public interface SessionRepository {

    /**
     * 保存用户登录会话
     *
     * <p>实现细节说明：</p>
     * <li>保存 ZSet：key=onlineKey，value=accessTokenId，score=登录时间，用于查询在线设备和踢人</li>
     * <li>保存 Hash：key=refreshToAccessKey，
     * field=refreshTokenId，value=accessTokenId，用于刷新 AccessToken</li>
     * <li>保存 Hash：key=accessToRefreshKey，
     * field=accessTokenId，value=refreshTokenId，用于失效 RefreshToken</li>
     * <li>设置 TTL：会话声明周期，不依赖 AccessToken 本身生命周期</li>
     *
     * @param campusId       校区ID
     * @param userId         用户ID
     * @param accessTokenId  访问令牌唯一标识（jti）
     * @param refreshTokenId 刷新令牌唯一标识（jti）
     */
    void saveSession(long campusId, long userId, String accessTokenId, String refreshTokenId);

    /**
     * 删除单个用户会话（单端注销或单设备踢出）
     *
     * <p>实现细节：</p>
     * <ul>
     *     <li>从 online ZSet 中移除该 token</li>
     *     <li>删除AccessTokenID和RefreshTokenID索引</li>
     *     <li>加入黑名单，防止 JWT 继续使用</li>
     * </ul>
     *
     * @param campusId      校区ID
     * @param userId        用户ID
     * @param accessTokenId 待删除的访问令牌唯一标识
     */
    void removeSession(long campusId, long userId, String accessTokenId);

    /**
     * 删除用户全部会话（管理员踢人 / 强制下线）
     *
     * <p>实现细节：</p>
     * <ul>
     *     <li>Lua脚本原子执行：递增用户令牌全局版本号，删除online ZSet、refresh双向映射等全部会话Redis数据</li>
     *     <li>依靠tokenVersion全局版本实现批量令牌失效，不再批量写入黑名单；
     *     refreshToken映射会被直接清除，无法刷新新凭证</li>
     *     <li>返回的tokenId集合为执行前查询快照，存在极小竞态窗口，，允许审计数据源有些许偏差</li>
     *     <li>安全说明：本方案无法拦截
     *     <strong>已泄露、且版本号匹配、尚未过期的AccessToken</strong>；
     *     缩短AccessToken有效期可缩小风险窗口，发现泄露可手动将单条tokenId加入黑名单处置</li>
     * </ul>
     *
     * @param campusId 校区ID
     * @param userId   用户ID
     * @return 被移除的访问令牌唯一标识集合
     */
    Set<String> removeAllSessions(long campusId, long userId);

    /**
     * 查询用户所有在线 AccessToken ID
     *
     * @return 当前用户在线的所有 AccessToken ID；无会话时返回空集合
     */
    Set<String> getAllSessions(long campusId, long userId);

    /**
     * 判断指定 AccessToken 是否在线
     * <p>用途：</p>
     * <ul>
     *     <li>检查用户是否仍然登录</li>
     *     <li>实现单点登录和多端登录限制逻辑</li>
     * </ul>
     *
     * @param campusId      校区ID
     * @param userId        用户ID
     * @param accessTokenId 访问令牌唯一标识
     * @return true 表示在线
     */
    boolean isOnline(long campusId, long userId, String accessTokenId);

    /**
     * 将 AccessToken 加入黑名单
     *
     * <p>用途：</p>
     * <ul>
     *     <li>强制注销 token，防止继续访问接口</li>
     *     <li>配合 removeSession 使用</li>
     * </ul>
     *
     * @param accessTokenId 访问令牌唯一标识
     */
    void addToBlacklist(String accessTokenId);

    /**
     * 判断指定 AccessToken 是否在黑名单
     *
     * @param accessTokenId 访问令牌唯一标识
     * @return true 表示已被拉黑
     */
    boolean isBlacklisted(String accessTokenId);

    /**
     * 统计当前在线会话数量
     *
     * <p>用途：</p>
     * <ul>
     *     <li>用于多端登录限制</li>
     *     <li>实现踢掉最早会话等策略</li>
     * </ul>
     * @return 当前在线的 AccessToken 数量
     */
    Long countSessions(long campusId, long userId);

    /**
     * 获取最早登录的 AccessToken
     *
     * <p>用途：</p>
     * <ul>
     *     <li>实现多端登录策略时，踢掉最早登录的设备</li>
     * </ul>
     *
     * @return 最早登录的 AccessToken ID；无会话时返回 {@link Optional#empty()}
     */
    Optional<String> getOldestSession(long campusId, long userId);

    /**
     * 获取用户当前的令牌版本号（不存在时初始化为 1）
     *
     * @return 令牌版本号
     */
    long getTokenVersion(long campusId, long userId);

    /**
     * 校验 RefreshToken 是否存在
     *
     * @param refreshTokenId 刷新令牌唯一标识
     * @return true 表示存在
     */
    boolean existsRefreshToken(long campusId, long userId, String refreshTokenId);

    /**
     * Refresh Token轮换
     * 删除旧refresh
     * 保存新refresh
     * @param oldRefreshTokenId 旧Refresh Token ID
     * @param oldAccessTokenId 旧Access Token ID
     * @param newRefreshTokenId 新Refresh Token ID
     * @param newAccessTokenId 新Access Token ID
     */
    void rotateRefreshToken(
        long campusId, long userId, String oldRefreshTokenId,
        String oldAccessTokenId, String newRefreshTokenId, String newAccessTokenId
    );

    /**
     * 通过RefreshToken ID获取 AccessToken ID
     * @param refreshTokenId Refresh Token ID
     * @return Access Token ID
     */
    String getAccessTokenIdByRefreshTokenId(long campusId, long userId, String refreshTokenId);

}
