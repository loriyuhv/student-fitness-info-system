package com.wsw.fitnesssystem.auth.domain.port;

import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;

import java.util.Optional;
import java.util.Set;

/**
 * 登录会话存储（Session Repository Port）
 *
 * <p>
 * 这是领域层定义的“能力接口”（Port），用于管理用户登录会话（AccessToken / RefreshToken）。
 * 注意：Domain 层只定义需要什么能力，而不关心具体实现。具体实现由 Infrastructure 层提供（如 RedisSessionRepository）。
 * </p>
 *
 * <p>主要职责：</p>
 * <ul>
 *     <li>保存用户登录会话信息（AccessToken / RefreshToken）</li>
 *     <li>支持查询会话是否在线</li>
 *     <li>支持删除单个或全部会话</li>
 *     <li>支持 AccessToken 黑名单管理</li>
 *     <li>支持获取当前在线会话数量和最早会话，用于多端登录控制</li>
 * </ul>
 *
 * <p>亮点：</p>
 * <ul>
 *     <li>遵循依赖反转原则（Domain 依赖接口，不依赖 Redis 或 JWT 实现）</li>
 *     <li>可替换存储实现（Redis / 内存 / 数据库等）</li>
 *     <li>支持多端登录限制、单点登录、Token 刷新、强制下线等功能</li>
 * </ul>
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
     * @param operator 操作对象
     * @param accessTokenId 当前登录的 AccessToken 唯一标识（jti）
     * @param refreshTokenId 当前登录的 RefreshToken 唯一标识（jti）
     */
    void saveSession(Operator operator, String accessTokenId, String refreshTokenId);

    /**
     * 删除单个用户会话（单端注销或踢人）
     *
     * <p>实现细节：</p>
     * <ul>
     *     <li>从 online ZSet 中移除该 token</li>
     *     <li>删除AccessTokenID和RefreshTokenID索引</li>
     *     <li>加入黑名单，防止 JWT 继续使用</li>
     * </ul>
     *
     * @param operator 操作对象
     * @param accessTokenId 要删除的 AccessToken ID
     */
    void removeSession(Operator operator, String accessTokenId);

    /**
     * 删除该用户全部会话（踢掉所有设备）
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
     * @param operator 操作对象
     */
    Set<String> removeAllSessions(Operator operator);

    /**
     * 获取用户所有在线 AccessToken ID
     *
     * @param operator 操作对象
     * @return 当前用户在线的所有 AccessToken ID
     */
    Set<String> getAllSessions(Operator operator);

    /**
     * 判断指定 token 是否在线
     * <p>用途：</p>
     * <ul>
     *     <li>检查用户是否仍然登录</li>
     *     <li>实现单点登录和多端登录限制逻辑</li>
     * </ul>
     * @param operator 操作对象
     * @param accessTokenId AccessToken ID
     * @return true 表示 token 仍在线，false 表示已下线或被踢
     */
    boolean isOnline(Operator operator, String accessTokenId);

    /**
     * 将指定 AccessToken 加入黑名单
     * <p>用途：</p>
     * <ul>
     *     <li>强制注销 token，防止继续访问接口</li>
     *     <li>配合 removeSession 使用</li>
     * </ul>
     * @param accessTokenId AccessToken ID
     */
    void addToBlacklist(String accessTokenId);

    /**
     * 判断指定 AccessToken 是否在黑名单
     *
     * @param accessTokenId AccessToken ID
     * @return true 表示在黑名单中，false 表示有效
     */
    boolean isBlacklisted(String accessTokenId);

    /**
     * 获取当前在线会话数量
     * <p>用途：</p>
     * <ul>
     *     <li>用于多端登录限制</li>
     *     <li>实现踢掉最早会话等策略</li>
     * </ul>
     * @param operator 操作对象
     * @return 当前在线的 AccessToken 数量
     */
    Long countSessions(Operator operator);

    /**
     * 获取最早登录的 AccessToken
     * <p>用途：</p>
     * <ul>
     *     <li>实现多端登录策略时，踢掉最早登录的设备</li>
     * </ul>
     * @param operator 操作对象
     * @return 最早登录的 AccessToken ID，若无返回 Optional.empty()
     */
    Optional<String> getOldestSession(Operator operator);

    /**
     * 获取用户当前版本号（如果不存在则初始化为 1）
     * @param operator 操作对象
     * @return token版本号
     */
    long getTokenVersion(Operator operator);

    /**
     * 校验refreshToken是否存在
     * @param operator 操作对象
     * @param refreshTokenId Refresh Token ID
     * @return 是否存在值
     */
    boolean existsRefreshToken(Operator operator, String refreshTokenId);

    /**
     * Refresh Token轮换
     * 删除旧refresh
     * 保存新refresh
     * @param operator 操作对象
     * @param oldRefreshTokenId 旧Refresh Token ID
     * @param oldAccessTokenId 旧Access Token ID
     * @param newRefreshTokenId 新Refresh Token ID
     * @param newAccessTokenId 新Access Token ID
     */
    void rotateRefreshToken(
            Operator operator,
            String oldRefreshTokenId,
            String oldAccessTokenId,
            String newRefreshTokenId,
            String newAccessTokenId
    );

    /**
     * 通过RefreshToken ID获取 AccessToken ID
     * @param operator 操作对象
     * @param refreshTokenId Refresh Token ID
     * @return Access Token ID
     */
    String getAccessTokenIdByRefreshTokenId(Operator operator, String refreshTokenId);

}
