package com.wsw.fitnesssystem.auth.infrastructure.persistence.redis.model;

/**
 * 认证授权Redis Key规范
 * <p>设计原则：</p>
 * <ul>
 *     <li>前缀区分业务域：auth:{子系统}:{业务}:{维度}</li>
 *     <li>多校区场景：所有用户数据（Key）必须包含 campusId，便于隔离和排查</li>
 *     <li>分层缓存：user / role / perm 解耦（RBAC）</li>
 *     <li>生命周期分离：不同数据不同 TTL</li>
 *     <li>可扩展性：支持未来权限版本、网关鉴权、SSO</li>
 * </ul>
 * @author loriyuhv
 * @version 1.0 2026/1/16 14:33
 * @since 1.0
 */
public class AuthRedisKeys {
    // ==================== 登录会话（在线状态）====================

    /**
     * 用户在线会话集合（ZSET）
     * <p>作用：统计用户在线数量、快速查找用户所有 AccessToken</p>
     * <li>Key: auth:session:online:{campusId}:{userId}</li>
     * <li>Field: {accessTokenId}</li>
     * <li>Value: 登录时间戳</li>
     * <li>TTL: 7天（refreshToken有效期）</li>
     */
    private static final String SESSION_ONLINE_PREFIX = "auth:session:online:";

    /**
     * 用户Refresh Token索引（Hash）
     * Key: auth:session:refresh:{campusId}:{userId}:{deviceId}
     * Field: {refreshTokenId}
     * Value: 关联的accessTokenId
     * TTL: 7天
     */
    private static final String SESSION_REFRESH_PREFIX = "auth:session:refresh:";

    // ==================== Token黑名单 ====================

    /**
     * JWT黑名单Key
     * Key: auth:session:blacklist:{accessTokenId}
     * TTL: accessToken有效期（如30分钟）
     */
    private static final String SESSION_BLACKLIST_PREFIX = "auth:session:blacklist:";

    // ==================== 权限缓存（全局共享）====================

    /**
     * 用户权限快照（String，JSON）
     * Key: auth:perm:user:{campusId}:{userId}
     * Value: UserAuthorization序列化
     * TTL: 30分钟
     * 注意：不按token隔离，用户所有设备共享权限
     * 权限变更时统一失效
     */
    private static final String PERM_USER_PREFIX = "auth:perm:user:";

    // ==================== Token版本控制 ====================

    /**
     * 用户Token版本号（String，整数）
     * Key: auth:version:user:{campusId}:{userId}
     * Value: 版本号（初始为1，每次需要全局失效时递增）
     * TTL: 永久（或随用户生命周期，由业务主动删除）
     * 用途：用于实现用户级Token全局失效（如修改密码、权限变更后强制所有设备下线）
     * 校验时比对Token中的版本号与Redis中的版本号，不一致则拒绝
     */
    private static final String TOKEN_VERSION_PREFIX = "auth:version:user:";

    // ==================== 登录安全（限流风控）====================

    /**
     * 登录失败计数（String）
     * Key: auth:limit:fail:{type}:{identifier}
     * type: user/ip
     * identifier: username 或 campusId:username 或 ip地址
     * TTL: 1分钟（滑动窗口）
     */
    private static final String LIMIT_FAIL_PREFIX = "auth:limit:fail:";

    /**
     * 锁定状态（String）
     * Key: auth:limit:lock:{type}:{identifier}
     * TTL: 30分钟
     */
    private static final String LIMIT_LOCK_PREFIX = "auth:limit:lock:";

    private AuthRedisKeys() {}

    // ==================== 在线会话 ====================

    /**
     * 用户在线会话Key
     * 存储该用户所有有效的accessTokenId
     */
    public static String onlineKey(Long campusId, Long userId) {
        return SESSION_ONLINE_PREFIX + campusId + ":" + userId;
    }

    /**
     * 用户Refresh Token索引Key
     */
    public static String refreshIndexKey(Long campusId, Long userId) {
        return SESSION_REFRESH_PREFIX + campusId + ":" + userId;
    }

    // ==================== Token黑名单 ====================
    /**
     * 获取黑名单Key
     */
    public static String blacklistKey(String accessTokenId) {
        return SESSION_BLACKLIST_PREFIX + accessTokenId;
    }

    // ==================== 权限缓存 ====================

    /**
     * 用户权限缓存Key（全局，不区分token）
     */
    public static String permUserKey(Long campusId, Long userId) {
        return PERM_USER_PREFIX + campusId + ":" + userId;
    }

    // ==================== Token版本控制 ====================

    /**
     * 用户Token版本号Key
     * <p>
     *     用于实现用户级Token全局失效，例如场景==>效果
     *     <li>修改密码 ==> 全部 Token 失效</li>
     *     <li>管理员踢人 ==> 全端下线</li>
     *     <li>风控封禁 ==> 秒级生效</li>
     * </p>
     * @param campusId 校区ID
     * @param userId 用户ID
     * @return Redis Key
     */
    public static String tokenVersionKey(Long campusId, Long userId) {
        return TOKEN_VERSION_PREFIX + campusId + ":" + userId;
    }

    // ==================== 登录限流 ====================

    /**
     * 登录失败计数Key（用户维度）
     * @param identifier campusId:username 或 username
     * @return key
     */
    public static String limitUserFailKey(String identifier) {

        return LIMIT_FAIL_PREFIX + "user:" + identifier;
    }


    /**
     * 用户锁定Key
     * @param identifier campusId:username 或 username
     * @return key
     */
    public static String limitUserLockKey(String identifier) {
        return LIMIT_LOCK_PREFIX + "user:" + identifier;
    }

    // ==================== 审计辅助 ====================
}
