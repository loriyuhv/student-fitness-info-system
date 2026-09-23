package com.wsw.fitnesssystem.iam.authorization.infrastructure.cache;

/**
 * Authorization 子域 Redis Key 工厂
 *
 * <p><b>职责：</b>集中定义授权缓存相关的 Redis Key 命名。</p>
 *
 * <p><b>Key 规范：</b>{@code iam:authz:{维度}:{campusId}:{userId}}</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/23 17:19
 * @since 1.0
 */
public class AuthorizationRedisKeys {

    /**
     * 用户权限快照（String，JSON）
     * <ul>
     *   <li>Key: {@code iam:authz:user:{campusId}:{userId}}</li>
     *   <li>Value: UserAuthorization 序列化</li>
     *   <li>TTL: 30 分钟</li>
     * </ul>
     *
     * <p><b>注意：</b>不按 token 隔离，用户所有设备共享权限；
     * 权限变更时统一失效。</p>
     */
    private static final String PERM_USER_PREFIX = "iam:authz:user:";

    private AuthorizationRedisKeys() {}

    /**
     * 用户权限缓存 Key
     */
    public static String permUserKey(long campusId, long userId) {
        return PERM_USER_PREFIX + campusId + ":" + userId;
    }

}
