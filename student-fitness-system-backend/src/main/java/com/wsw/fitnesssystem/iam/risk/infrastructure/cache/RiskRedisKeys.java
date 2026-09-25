package com.wsw.fitnesssystem.iam.risk.infrastructure.cache;

/**
 * Risk 子域 Redis Key 工厂
 *
 * <p><b>职责：</b>集中定义登录风控相关的 Redis Key 命名。</p>
 *
 * <p><b>Key 规范：</b>{@code iam:risk:{维度}:{type}:{identifier}}</p>
 *
 * <p><b>identifier 说明：</b>
 * <ul>
 *   <li>type = user 时：identifier 为 username</li>
 *   <li>type = ip 时：identifier 为 IP 地址（预留）</li>
 * </ul></p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/23 17:20
 * @since 1.0
 */
public final class RiskRedisKeys {

    /**
     * 登录失败计数（String，整数）
     * <ul>
     *   <li>Key: {@code iam:risk:fail:user:{username}}</li>
     *   <li>TTL: 与 {@code iam.risk.count-window-seconds} 一致</li>
     * </ul>
     */
    private static final String FAIL_PREFIX = "iam:risk:fail:user:";

    /**
     * 账号锁定标记（String）
     * <ul>
     *   <li>Key: {@code iam:risk:lock:user:{username}}</li>
     *   <li>TTL: 与 {@code iam.risk.lock-duration-seconds} 一致</li>
     * </ul>
     */
    private static final String LOCK_PREFIX = "iam:risk:lock:user:";

    private RiskRedisKeys() {}

    /**
     * 登录失败计数 Key
     */
    public static String riskUserFailKey(String username) {
        return FAIL_PREFIX + username;
    }

    /**
     * 账号锁定标记 Key
     */
    public static String riskUserLockKey(String username) {
        return LOCK_PREFIX + username;
    }

}
