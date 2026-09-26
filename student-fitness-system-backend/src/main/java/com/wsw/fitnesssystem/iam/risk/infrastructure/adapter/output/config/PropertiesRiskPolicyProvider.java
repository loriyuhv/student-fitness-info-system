package com.wsw.fitnesssystem.iam.risk.infrastructure.adapter.output.config;

import com.wsw.fitnesssystem.iam.risk.domain.enums.RiskDimension;
import com.wsw.fitnesssystem.iam.risk.domain.port.output.RiskPolicyProvider;
import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskPolicy;
import com.wsw.fitnesssystem.iam.risk.infrastructure.config.RiskPolicyProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * 基于配置文件的风控策略提供者实现。
 *
 * <p><b>职责：</b>启动时从 {@link RiskPolicyProperties} 读取所有维度配置，
 * 组装为领域值对象 {@link RiskPolicy} 并按维度缓存；运行时按维度 O(1) 返回。</p>
 *
 * <p><b>Fail-Fast：</b>若某个维度在配置中缺失，应用启动即失败，
 * 避免运行期才暴露问题。</p>
 *
 * <p><b>防御：</b>配置层的 {@code @Validated} 已保证参数合法，
 * 组装时不会再触发值对象的校验异常；若触发，说明配置层校验被绕过。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/27 12:59
 * @since 1.0
 */
@Component
public class PropertiesRiskPolicyProvider implements RiskPolicyProvider {

    /** 预构建的按维度策略缓存，启动后不可变 */
    private final Map<RiskDimension, RiskPolicy> policyCache;

    public PropertiesRiskPolicyProvider(RiskPolicyProperties properties) {
        if (properties.getPolicies().isEmpty()) {
            throw new IllegalStateException(
                "No risk policy configured. Please set 'iam.risk.policies.*' in application.yaml.");
        }

        Map<RiskDimension, RiskPolicy> cache = new EnumMap<>(RiskDimension.class);

        for (RiskDimension dimension : RiskDimension.values()) {
            RiskPolicyProperties.Policy cfg = properties.getPolicies().get(dimension);

            // Fail-fast：缺失配置直接启动失败
            if (cfg == null) {
                String key = dimension.name().toLowerCase();
                throw new IllegalStateException(
                    "Missing risk policy config for dimension: " + key
                        + ". Please configure 'iam.risk.policies." + key + "'.");
            }

            cache.put(dimension, new RiskPolicy(
                cfg.getMaxFailCount(),
                cfg.getLockDurationSeconds(),
                cfg.getCountWindowSeconds()
            ));
        }

        this.policyCache = Collections.unmodifiableMap(cache);
    }

    @Override
    public RiskPolicy getPolicy(RiskDimension dimension) {
        RiskPolicy policy = policyCache.get(dimension);
        // 理论上不会为 null（构造函数已 fail-fast 全维度覆盖）
        if (policy == null) {
            throw new IllegalStateException("No policy registered for dimension: " + dimension);
        }
        return policy;
    }

}
