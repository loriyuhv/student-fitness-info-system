package com.wsw.fitnesssystem.auth.risk.domain.valueobject;

/**
 * 风控策略 - 值对象
 *
 * <p>不可变。由应用层根据外部配置构建后传入领域层。
 * 领域层用策略参数做判断，但不关心配置从哪来。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:08
 * @since 1.0
 */
public record RiskPolicy(int maxFailCount, long lockDurationSeconds, long countWindowSeconds) {

    public RiskPolicy {
        if (maxFailCount <= 0) {
            throw new IllegalArgumentException("maxFailCount must > 0, got: " + maxFailCount);
        }
        if (lockDurationSeconds <= 0) {
            throw new IllegalArgumentException("lockDurationSeconds must > 0, got: " + lockDurationSeconds);
        }
        if (countWindowSeconds <= 0) {
            throw new IllegalArgumentException("countWindowSeconds must > 0, got: " + countWindowSeconds);
        }
    }

}
