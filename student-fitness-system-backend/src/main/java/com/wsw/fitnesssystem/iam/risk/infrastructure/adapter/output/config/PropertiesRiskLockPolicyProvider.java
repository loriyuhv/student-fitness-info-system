package com.wsw.fitnesssystem.iam.risk.infrastructure.adapter.output.config;

import com.wsw.fitnesssystem.iam.risk.domain.port.output.RiskLockPolicyProvider;
import com.wsw.fitnesssystem.iam.risk.infrastructure.config.RiskLockPolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 基于配置文件的风控锁定策略实现
 *
 * @author loriyuhv
 * @version 1.0 2026/8/27 12:59
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class PropertiesRiskLockPolicyProvider implements RiskLockPolicyProvider {

    private final RiskLockPolicyProperties riskPolicyProperties;

    @Override
    public int getMaxFailCount() {
        return riskPolicyProperties.getMaxFailCount();
    }

    @Override
    public long getLockDurationSeconds() {
        return riskPolicyProperties.getLockDurationSeconds();
    }

    @Override
    public long getCountWindowSeconds() {
        return riskPolicyProperties.getCountWindowSeconds();
    }

}
