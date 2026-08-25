package com.wsw.fitnesssystem.auth.authentication.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/24 11:08
 * @since 1.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "auth.security")
public class SecurityProperties {

    /**
     * 无需认证放行接口（servletPath，不带context‑path前缀）
     */
    private List<String> permitAllPatterns;

    /**
     * BCrypt加密强度 4‑31
     */
    private Integer bcryptStrength;

}
