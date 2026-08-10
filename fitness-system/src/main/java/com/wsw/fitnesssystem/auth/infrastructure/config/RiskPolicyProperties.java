package com.wsw.fitnesssystem.auth.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 风控策略配置属性
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:25
 * @since 1.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "auth.risk")
public class RiskPolicyProperties {
    /** 最大失败次数阈值 */
    private int maxFailCount = 5;

    /** 锁定持续时间（分钟） */
    private int lockDurationMinutes = 30;

    /** 失败计数窗口（分钟） */
    private int countWindowMinutes = 30;
}
