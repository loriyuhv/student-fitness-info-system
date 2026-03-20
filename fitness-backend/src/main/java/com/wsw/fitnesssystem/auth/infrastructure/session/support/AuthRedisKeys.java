package com.wsw.fitnesssystem.auth.infrastructure.session.support;

/**
 * 认证授权Redis Key规范
 * 设计原则：
 * 1. 前缀区分业务域：auth:{子系统}:{业务}:{维度}
 * 2. 多校区场景：key包含campusId，便于隔离和排查
 * 3. 生命周期匹配：不同TTL的Key分开存储
 * @author loriyuhv
 * @version 1.0 2026/1/16 14:33
 * @since 1.0
 */
public class AuthRedisKeys {
    // ==================== 登录会话（在线状态）====================

    /**
     * 用户在线会话集合（ZSET）
     * Key: auth:session:online:{campusId}:{userId}
     * Field: {tokenId}
     * Value: 登录时间戳
     * TTL: 7天（refreshToken有效期）
     */
    private static final String SESSION_ONLINE_PREFIX = "auth:session:online:";

    /**
     * 用户Refresh Token索引（Hash）
     * Key: auth:session:refresh:{campusId}:{userId}
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

    /**
     * 角色权限映射（String，Hash）
     * Key: auth:perm:role:{roleCode}
     * Value: 权限编码集合
     * TTL: 1小时（角色权限变更较少）
     */
    private static final String PERM_ROLE_PREFIX = "auth:perm:role:";

    // ==================== 登录安全（限流风控）====================

    /**
     * 登录失败计数（String）
     * Key: auth:limit:fail:{type}:{identifier}
     * type: user/ip
     * identifier: username 或 ip地址
     * TTL: 1分钟（滑动窗口）
     */
    private static final String LIMIT_FAIL_PREFIX = "auth:limit:fail:";

    /**
     * 锁定状态（String）
     * Key: auth:limit:lock:{type}:{identifier}
     * TTL: 30分钟
     */
    private static final String LIMIT_LOCK_PREFIX = "auth:limit:lock:";

    // ==================== 审计辅助（可选）====================

    /**
     * 用户登录设备列表（Set）
     * Key: auth:audit:devices:{campusId}:{userId}
     * Value: 设备指纹集合
     * TTL: 7天
     */
    private static final String AUDIT_DEVICES_PREFIX = "auth:audit:devices:";

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

    /**
     * 角色权限映射Key
     */
    public static String permRoleKey(String roleCode) {
        return PERM_ROLE_PREFIX + roleCode;
    }

    // ==================== 登录限流 ====================

    /**
     * 登录失败计数Key
     * @param type "user" 或 "ip"
     * @param identifier username 或 ip地址
     */
    public static String limitFailKey(String type, String identifier) {
        return LIMIT_FAIL_PREFIX + type + ":" + identifier;
    }

    /**
     * 账号/IP锁定Key
     */
    public static String limitLockKey(String type, String identifier) {
        return LIMIT_LOCK_PREFIX + type + ":" + identifier;
    }

    // ==================== 审计辅助 ====================

    /**
     * 用户设备列表Key（用于异常登录检测）
     */
    public static String auditDevicesKey(Long campusId, Long userId) {
        return AUDIT_DEVICES_PREFIX + campusId + ":" + userId;
    }
}
