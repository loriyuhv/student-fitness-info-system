package com.wsw.fitnesssystem.auth.authorization.infrastructure.persistence.redis.model;

/**
 * 认证授权Redis Key规范
 * <p>设计原则：</p>
 * <ul>
 *     <li>前缀区分业务域：auth:{子系统}:{业务}:{维度}</li>
 *     <li>多学校场景：所有用户数据（Key）必须包含 campusId，便于隔离和排查</li>
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
     * <li>Key: auth:session:refresh:{campusId}:{userId}</li>
     * <li>Field: {refreshTokenId}</li>
     * <li>Value: 关联的accessTokenId</li>
     * <li>TTL: 7天</li>
     */
    private static final String SESSION_REFRESH_TO_ACCESS_PREFIX = "auth:session:refresh2access:";

    /**
     * 用户AccessToken索引（Hash）
     * <li>Key：auth:session:access2refresh:{campusId}:{userId}</li>
     * <li>Field：{accessTokenId}</li>
     * <li>Value：关联的refreshTokenId</li>
     * <li>TTL：7天</li>
     */
    private static final String SESSION_ACCESS_TO_REFRESH_PREFIX = "auth:session:access2refresh:";

    // ==================== Token黑名单 ====================

    /**
     * JWT黑名单Key
     * <li>Key: auth:session:blacklist:{accessTokenId}</li>
     * <li>TTL: accessToken有效期（如30分钟）</li>
     */
    private static final String SESSION_BLACKLIST_PREFIX = "auth:session:blacklist:";

    // ==================== 权限缓存（全局共享）====================

    /**
     * 用户权限快照（String，JSON）
     * <li>Key: auth:perm:user:{campusId}:{userId}</li>
     * <li>Value: UserAuthorization序列化</li>
     * <li>TTL: 30分钟</li>
     * <p>注意：不按token隔离，用户所有设备共享权限，权限变更时统一失效</p>
     */
    private static final String PERM_USER_PREFIX = "auth:perm:user:";

    // ==================== Token版本控制 ====================

    /**
     * 用户Token版本号（String，整数）
     * <li>Key: auth:version:user:{campusId}:{userId}</li>
     * <li>Value: 版本号（初始为1，每次需要全局失效时递增）</li>
     * <li>TTL: 永久（或随用户生命周期，由业务主动删除）</li>
     * 用途：用于实现用户级Token全局失效（如修改密码、权限变更后强制所有设备下线）
     * 校验时比对Token中的版本号与Redis中的版本号，不一致则拒绝
     */
    private static final String TOKEN_VERSION_PREFIX = "auth:version:user:";

    // ==================== 登录安全（限流风控）====================

    /**
     * 登录失败计数（String）
     * <li>Key: auth:risk:fail:{type}:{identifier}</li>
     * <li>type: user/ip</li>
     * <li>identifier: username 或 campusId:username 或 ip地址</li>
     * <li>TTL: 1分钟（滑动窗口）</li>
     */
    private static final String RISK_FAIL_PREFIX = "auth:risk:fail:";

    /**
     * 锁定状态（String）
     * <li>Key: auth:risk:lock:{type}:{identifier}</li>
     * <li>TTL: 30分钟</li>
     */
    private static final String RISK_LOCK_PREFIX = "auth:risk:lock:";

    private AuthRedisKeys() {}

    // ==================== 在线会话 ====================

    /**
     * 用户在线会话Key
     * 存储该用户所有有效的accessTokenId
     */
    public static String onlineKey(long campusId, long userId) {
        return SESSION_ONLINE_PREFIX + campusId + ":" + userId;
    }

    /**
     * refreshToken -> accessToken hash key
     * @param campusId 学校ID
     * @param userId 用户ID
     * @return redis key
     */
    public static String refreshToAccessKey(long campusId, long userId) {
        return SESSION_REFRESH_TO_ACCESS_PREFIX + campusId + ":" + userId;
    }

    /**
     * accessToken -> refreshToken hash key
     * @return redis key
     */
    public static String accessToRefreshKey(long campusId, long userId) {
        return SESSION_ACCESS_TO_REFRESH_PREFIX + campusId + ":" + userId;
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
    public static String permUserKey(long userId, long campusId) {
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
     * @return Redis Key
     */
    public static String tokenVersionKey(long campusId, long userId) {
        return TOKEN_VERSION_PREFIX + campusId + ":" + userId;
    }

    // ==================== 登录限流 ====================

    /**
     * 登录失败计数Key（用户维度）
     * @param identifier campusId:username 或 username
     * @return key
     */
    public static String riskUserFailKey(String identifier) {

        return RISK_FAIL_PREFIX + "user:" + identifier;
    }


    /**
     * 用户锁定Key
     * @param identifier campusId:username 或 username
     * @return key
     */
    public static String riskUserLockKey(String identifier) {
        return RISK_LOCK_PREFIX + "user:" + identifier;
    }

    // ==================== 审计辅助 ====================
}
