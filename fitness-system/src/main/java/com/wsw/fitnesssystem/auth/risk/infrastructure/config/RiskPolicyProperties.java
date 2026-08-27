package com.wsw.fitnesssystem.auth.risk.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 风控策略配置属性
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:25
 * @since 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth.risk")
public class RiskPolicyProperties {
    /** 最大失败次数阈值 */
    private int maxFailCount = 3;

    /** 锁定持续时间（分钟） */
    private int lockDurationMinutes = 30;

    /** 失败计数窗口（分钟） */
    private int countWindowMinutes = 30;
}
