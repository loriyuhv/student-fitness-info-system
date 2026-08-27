package com.wsw.fitnesssystem.auth.risk.domain.policy;

/**
 * 风控锁定策略（Domain 层定义）
 *
 * @author loriyuhv
 * @version 1.0 2026/8/27 12:51
 * @since 1.0
 */
public interface RiskLockPolicy {

    int getMaxFailCount();
    int getLockDurationMinutes();
    int getCountWindowMinutes();

}
