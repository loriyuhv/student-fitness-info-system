package com.wsw.fitnesssystem.auth.risk.domain.policy;

/**
 * 风控锁定策略（Domain 层定义）
 *
 * <p>定义如何获取风控策略参数，由 Infrastructure 层实现。
 * 目的是隔离 Application 层对配置类的直接依赖。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/27 12:51
 * @since 1.0
 */
public interface RiskLockPolicy {

    int getMaxFailCount();

    long getLockDurationSeconds();

    long getCountWindowSeconds();

}
