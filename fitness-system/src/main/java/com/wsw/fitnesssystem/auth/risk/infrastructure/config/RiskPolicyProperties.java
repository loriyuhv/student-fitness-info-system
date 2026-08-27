package com.wsw.fitnesssystem.auth.risk.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 风控策略配置属性
 *
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

    /** 锁定持续时间（秒） */
    private long lockDurationSeconds = 900;

    /** 失败计数窗口（秒） */
    private long countWindowSeconds = 900;

}
