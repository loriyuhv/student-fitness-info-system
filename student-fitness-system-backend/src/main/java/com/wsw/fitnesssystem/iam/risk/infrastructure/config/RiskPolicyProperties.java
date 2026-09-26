package com.wsw.fitnesssystem.iam.risk.infrastructure.config;

import com.wsw.fitnesssystem.iam.risk.domain.enums.RiskDimension;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

/**
 * 风控策略配置属性（按维度分组）
 *
 * <p><b>配置结构：</b>
 * <pre>{@code
 * iam:
 *   risk:
 *     policies:
 *       user:
 *         max-fail-count: 6
 *         lock-duration-seconds: 900
 *         count-window-seconds: 900
 *       ip:
 *         max-fail-count: 30
 *         lock-duration-seconds: 300
 *         count-window-seconds: 60
 * }</pre></p>
 *
 * <p><b>启动期校验：</b>通过 {@code @Validated} + JSR 303，
 * 配置非法时应用启动失败。这是主防线，把配置错误挡在启动期。</p>
 *
 * <p><b>与 {@code RiskPolicy} 的分工：</b>
 * <ul>
 *   <li>本类：启动期拦截「application.yaml 配置错误」</li>
 *   <li>{@code RiskPolicy}：兜底防线，防止任何非法值进入领域层</li>
 * </ul></p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:25
 * @since 1.0
 */
@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "iam.risk")
public class RiskPolicyProperties {

    /**
     * 按维度分组的策略配置。
     * <p>Key 为 {@link RiskDimension} 枚举（Spring Boot 绑定支持
     * {@code user} / {@code USER} 等宽松写法），Value 为该维度的策略参数。</p>
     */
    private Map<RiskDimension, Policy> policies = new HashMap<>();

    /**
     * 单个维度的策略参数。
     */
    @Getter
    @Setter
    public static class Policy {
        /** 最大失败次数阈值（必须 ≥ 1） */
        @Min(value = 1, message = "iam.risk.policies.*.max-fail-count 必须 >= 1")
        private int maxFailCount;

        /** 锁定持续时间（秒，必须 ≥ 1） */
        @Min(value = 1, message = "iam.risk.policies.*.lock-duration-seconds 必须 >= 1")
        private long lockDurationSeconds;

        /** 失败计数窗口（秒，必须 ≥ 1） */
        @Min(value = 1, message = "iam.risk.policies.*.count-window-seconds 必须 >= 1")
        private long countWindowSeconds;
    }

}
