package com.wsw.fitnesssystem.iam.risk.infrastructure.adapter.output.config;

import com.wsw.fitnesssystem.iam.risk.domain.port.output.RiskLockPolicyProvider;
import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskLockPolicy;
import com.wsw.fitnesssystem.iam.risk.infrastructure.config.RiskLockPolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 基于配置文件的风控锁定策略实现。
 *
 * <p><b>职责：</b>从配置读取参数，组装为领域值对象 {@link RiskLockPolicy}。</p>
 *
 * <p><b>防御：</b>配置层的 {@code @Validated} 已保证参数合法，
 * 本类组装时不会再触发值对象的校验异常。若触发，说明配置层校验被绕过。</p>
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
    public RiskLockPolicy getPolicy() {
        return new RiskLockPolicy(
            riskPolicyProperties.getMaxFailCount(),
            riskPolicyProperties.getLockDurationSeconds(),
            riskPolicyProperties.getCountWindowSeconds()
        );
    }

}
