package com.wsw.fitnesssystem.iam.risk.infrastructure.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 风控策略配置属性
 *
 * <p><b>启动期校验：</b>通过 {@code @Validated} + JSR 303 注解，
 * 配置非法时应用启动失败。这是<b>主防线</b>——把配置错误挡在启动期，
 * 而非运行时。</p>
 *
 * <p><b>与 {@code RiskLockPolicy} 的分工：</b></p>
 * <ul>
 *   <li>本类的校验：<b>启动期</b>拦截「application.yaml 配置错误」</li>
 *   <li>{@code RiskLockPolicy} 的校验：<b>兜底防线</b>，防止任何非法值
 *       进入领域层</li>
 * </ul>
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
public class RiskLockPolicyProperties {

    /** 最大失败次数阈值（必须 ≥ 1） */
    @Min(value = 1, message = "iam.risk.max-fail-count 必须 >= 1")
    private int maxFailCount = 3;

    /** 锁定持续时间（秒，必须 ≥ 1） */
    @Min(value = 1, message = "iam.risk.lock-duration-seconds 必须 >= 1")
    private long lockDurationSeconds = 900;

    /** 失败计数窗口（秒，必须 ≥ 1） */
    @Min(value = 1, message = "iam.risk.count-window-seconds 必须 >= 1")
    private long countWindowSeconds = 900;

}
