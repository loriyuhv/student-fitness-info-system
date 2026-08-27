package com.wsw.fitnesssystem.auth.risk.infrastructure.policy;

import com.wsw.fitnesssystem.auth.risk.domain.policy.RiskLockPolicy;
import com.wsw.fitnesssystem.auth.risk.infrastructure.config.RiskPolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/27 12:59
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class ConfigBasedRiskLockPolicy implements RiskLockPolicy {

    private final RiskPolicyProperties riskPolicyProperties;

    @Override
    public int getMaxFailCount() {
        return riskPolicyProperties.getMaxFailCount();
    }

    @Override
    public int getLockDurationMinutes() {
        return riskPolicyProperties.getLockDurationMinutes();
    }

    @Override
    public int getCountWindowMinutes() {
        return riskPolicyProperties.getCountWindowMinutes();
    }

}
