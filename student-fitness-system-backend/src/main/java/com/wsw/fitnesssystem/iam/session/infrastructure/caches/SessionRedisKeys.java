package com.wsw.fitnesssystem.iam.session.infrastructure.caches;

/**
 * Session 子域 Redis Key 工厂
 *
 * <p><b>职责：</b>集中定义会话管理相关的 Redis Key 命名，
 * 供 {@code RedisSessionRepository} 使用。</p>
 *
 * <p><b>Key 规范：</b>{@code iam:session:{维度}:{campusId}:{userId}}</p>
 *
 * <p><b>设计原则：</b>
 * <ul>
 *   <li>多校区隔离：所有用户相关的 Key 必带 campusId</li>
 *   <li>生命周期分离：不同数据不同 TTL</li>
 *   <li>集中管理：Key 前缀与拼接逻辑只在本类出现，禁止散落到 Repository</li>
 * </ul></p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/23 17:17
 * @since 1.0
 */
public class SessionRedisKeys {

    // ==================== Key 前缀 ====================

    /**
     * 用户在线会话集合（ZSET）
     * <ul>
     *   <li>Key: {@code iam:session:online:{campusId}:{userId}}</li>
     *   <li>Member: accessTokenId</li>
     *   <li>Score: 登录时间戳（毫秒）</li>
     *   <li>TTL: 7 天（与 refreshToken 有效期对齐）</li>
     * </ul>
     */
    private static final String ONLINE_PREFIX = "iam:session:online:";

    /**
     * RefreshToken → AccessToken 映射（Hash）
     * <ul>
     *   <li>Key: {@code iam:session:refresh2access:{campusId}:{userId}}</li>
     *   <li>Field: refreshTokenId</li>
     *   <li>Value: accessTokenId</li>
     *   <li>TTL: 7 天</li>
     * </ul>
     */
    private static final String REFRESH_TO_ACCESS_PREFIX = "iam:session:refresh2access:";

    /**
     * AccessToken → RefreshToken 映射（Hash）
     * <ul>
     *   <li>Key: {@code iam:session:access2refresh:{campusId}:{userId}}</li>
     *   <li>Field: accessTokenId</li>
     *   <li>Value: refreshTokenId</li>
     *   <li>TTL: 7 天</li>
     * </ul>
     */
    private static final String ACCESS_TO_REFRESH_PREFIX = "iam:session:access2refresh:";

    /**
     * AccessToken 黑名单（String）
     * <ul>
     *   <li>Key: {@code iam:session:blacklist:{accessTokenId}}</li>
     *   <li>TTL: 与 accessToken 剩余有效期一致</li>
     * </ul>
     */
    private static final String BLACKLIST_PREFIX = "iam:session:blacklist:";

    /**
     * 用户 Token 版本号（String，整数）
     * <ul>
     *   <li>Key: {@code iam:session:version:{campusId}:{userId}}</li>
     *   <li>用途：全局/单用户令牌失效（修改密码、踢人、风控封禁）</li>
     *   <li>TTL: 永久（随用户生命周期，由业务显式删除）</li>
     * </ul>
     */
    private static final String TOKEN_VERSION_PREFIX = "iam:session:version:";

    private SessionRedisKeys() {}

    // ==================== Key 工厂 ====================

    /**
     * 用户在线会话集合 Key
     */
    public static String onlineKey(long campusId, long userId) {
        return ONLINE_PREFIX + campusId + ":" + userId;
    }

    /**
     * RefreshToken → AccessToken 映射 Key
     */
    public static String refreshToAccessKey(long campusId, long userId) {
        return REFRESH_TO_ACCESS_PREFIX + campusId + ":" + userId;
    }

    /**
     * AccessToken → RefreshToken 映射 Key
     */
    public static String accessToRefreshKey(long campusId, long userId) {
        return ACCESS_TO_REFRESH_PREFIX + campusId + ":" + userId;
    }

    /**
     * AccessToken 黑名单 Key
     */
    public static String blacklistKey(String accessTokenId) {
        return BLACKLIST_PREFIX + accessTokenId;
    }

    /**
     * 用户 Token 版本号 Key
     */
    public static String tokenVersionKey(long campusId, long userId) {
        return TOKEN_VERSION_PREFIX + campusId + ":" + userId;
    }

}
