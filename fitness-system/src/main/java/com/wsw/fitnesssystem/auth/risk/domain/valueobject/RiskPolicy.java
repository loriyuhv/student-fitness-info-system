package com.wsw.fitnesssystem.auth.risk.domain.valueobject;

import com.wsw.fitnesssystem.auth.risk.infrastructure.config.RiskPolicyProperties;

/**
 * 风控策略 - 值对象
 *
 * <p>不可变。由应用层根据外部配置构建后传入领域层。
 * 领域层用策略参数做判断，但不关心配置从哪来。</p>
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:08
 * @since 1.0
 */
public record RiskPolicy(
        int maxFailCount,
        int lockDurationMinutes,
        int countWindowMinutes
) {
    public RiskPolicy {
        if (maxFailCount <= 0) {
            throw new IllegalArgumentException("maxFailCount must > 0, got: " + maxFailCount);
        }
        if (lockDurationMinutes <= 0) {
            throw new IllegalArgumentException("lockDurationMinutes must > 0, got: " + lockDurationMinutes);
        }
        if (countWindowMinutes <= 0) {
            throw new IllegalArgumentException("countWindowMinutes must > 0, got: " + countWindowMinutes);
        }
    }

    /** 静态工厂：从配置属性构建 */
    public static RiskPolicy fromProperties(RiskPolicyProperties props) {
        return new RiskPolicy(
                props.getMaxFailCount(),
                props.getLockDurationMinutes(),
                props.getCountWindowMinutes()
        );
    }

}
