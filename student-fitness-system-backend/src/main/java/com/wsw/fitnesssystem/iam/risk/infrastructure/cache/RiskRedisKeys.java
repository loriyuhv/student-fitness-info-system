package com.wsw.fitnesssystem.iam.risk.infrastructure.cache;

import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskSubject;

/**
 * Risk 子域 Redis Key 工厂
 *
 * <p><b>Key 规范：</b>{@code iam:risk:{type}:{dimension}:{identifier}}</p>
 *
 * <p><b>示例：</b>
 * <ul>
 *   <li>{@code iam:risk:fail:user:zhangsan}</li>
 *   <li>{@code iam:risk:lock:user:zhangsan}</li>
 *   <li>{@code iam:risk:fail:ip:1.2.3.4}</li>
 *   <li>{@code iam:risk:lock:device:abcd1234}</li>
 * </ul></p>
 *
 * <p><b>扩展：</b>新增维度无需修改本类，维度名由 {@link RiskSubject#dimension()} 决定。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/23 17:20
 * @since 1.0
 */
public final class RiskRedisKeys {

    private static final String PREFIX = "iam:risk:";
    private static final String FAIL_TYPE = "fail";
    private static final String LOCK_TYPE = "lock";

    private RiskRedisKeys() {}

    /**
     * 失败计数 Key。
     *
     * <p>Key格式：{@code iam:risk:fail:{dimension}:{value}}</p>
     */
    public static String failKey(RiskSubject subject) {
        return PREFIX + FAIL_TYPE + ":" + dimensionPart(subject) + ":" + subject.value();
    }

    /**
     * 锁定标记 Key。
     *
     * <p>格式：{@code iam:risk:lock:{dimension}:{value}}</p>
     */
    public static String lockKey(RiskSubject subject) {
        return PREFIX + LOCK_TYPE + ":" + dimensionPart(subject) + ":" + subject.value();
    }

    private static String dimensionPart(RiskSubject subject) {
        return subject.dimension().name().toLowerCase();
    }

}
