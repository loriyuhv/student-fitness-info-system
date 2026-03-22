package com.wsw.fitnesssystem.auth.domain.port;

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
     * @param campusId 校区ID，多租户隔离
     * @param userId 用户ID
     * @param accessTokenId 当前登录的 AccessToken 唯一标识（jti）
     * @param refreshTokenId 当前登录的 RefreshToken 唯一标识（jti）
     *
     * <p>实现细节说明：</p>
     * <ul>
     *     <li>保存 ZSet：key=onlineKey，value=accessTokenId，score=登录时间，用于查询在线设备和踢人</li>
     *     <li>保存 Hash：key=refreshIndexKey，field=refreshTokenId，value=accessTokenId，用于刷新 Token</li>
     *     <li>设置 TTL：会话声明周期，不依赖 AccessToken 本身生命周期</li>
     * </ul>
     */
    void saveSession(
        Long campusId,
        Long userId,
        String accessTokenId,
        String refreshTokenId
    );

    /**
     * 删除单个用户会话（单端注销或踢人）
     *
     * @param campusId 校区ID
     * @param userId 用户ID
     * @param accessTokenId 要删除的 AccessToken ID
     *
     * <p>实现细节：</p>
     * <ul>
     *     <li>从 online ZSet 中移除该 token</li>
     *     <li>加入黑名单，防止 JWT 继续使用</li>
     * </ul>
     */
    void removeSession(
        Long campusId,
        Long userId,
        String accessTokenId
    );

    /**
     * 删除该用户全部会话（踢掉所有设备）
     *
     * @param campusId 校区ID
     * @param userId 用户ID
     *
     * <p>实现细节：</p>
     * <ul>
     *     <li>删除 online ZSet 和 refresh Hash</li>
     *     <li>通常用于用户主动退出或管理员强制下线</li>
     * </ul>
     */
    void removeAll(Long campusId, Long userId);

    /**
     * 获取用户所有在线 AccessToken ID
     *
     * @param campusId 校区ID
     * @param userId 用户ID
     * @return 当前用户在线的所有 AccessToken ID
     */
    Set<String> getAllSessions(Long campusId, Long userId);

    /**
     * 判断指定 token 是否在线
     *
     * @param campusId 校区ID
     * @param userId 用户ID
     * @param accessTokenId AccessToken ID
     * @return true 表示 token 仍在线，false 表示已下线或被踢
     *
     * <p>用途：</p>
     * <ul>
     *     <li>检查用户是否仍然登录</li>
     *     <li>实现单点登录和多端登录限制逻辑</li>
     * </ul>
     */
    boolean isOnline(
        Long campusId,
        Long userId,
        String accessTokenId
    );

    /**
     * 将指定 AccessToken 加入黑名单
     *
     * @param accessTokenId AccessToken ID
     * @param expireSeconds 黑名单 TTL（秒），通常等于 AccessToken 生命周期
     *
     * <p>用途：</p>
     * <ul>
     *     <li>强制注销 token，防止继续访问接口</li>
     *     <li>配合 removeSession 使用</li>
     * </ul>
     */
    void addToBlacklist(String accessTokenId, long expireSeconds);

    /**
     * 判断指定 AccessToken 是否在黑名单
     *
     * @param accessTokenId AccessToken ID
     * @return true 表示在黑名单中，false 表示有效
     */
    boolean isBlacklisted(String accessTokenId);

    /**
     * 获取当前在线会话数量
     *
     * @param campusId 校区ID
     * @param userId 用户ID
     * @return 当前在线的 AccessToken 数量
     *
     * <p>用途：</p>
     * <ul>
     *     <li>用于多端登录限制</li>
     *     <li>实现踢掉最早会话等策略</li>
     * </ul>
     */
    Long countSessions(Long campusId, Long userId);

    /**
     * 获取最早登录的 AccessToken
     *
     * @param campusId 校区ID
     * @param userId 用户ID
     * @return 最早登录的 AccessToken ID，若无返回 Optional.empty()
     *
     * <p>用途：</p>
     * <ul>
     *     <li>实现多端登录策略时，踢掉最早登录的设备</li>
     * </ul>
     */
    Optional<String> getOldestSession(Long campusId, Long userId);
}
