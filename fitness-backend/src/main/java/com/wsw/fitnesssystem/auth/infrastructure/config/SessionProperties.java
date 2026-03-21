package com.wsw.fitnesssystem.auth.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 10:42
 * @since 1.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "auth.session")
public class SessionProperties {
    /** 登录态过期时间（ZSet / Hash） */
    private long expireMillis;

    /** AccessToken 过期时间（JWT） */
    private long accessTokenExpireMinutes;

    /** RefreshToken 过期时间（JWT） */
    private long refreshTokenExpireMillis;
}
