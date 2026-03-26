package com.wsw.fitnesssystem.auth.infrastructure.config;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT 相关配置与密钥初始化
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 16:09
 * @since 1.0
 */
@Slf4j
@Getter
@Configuration
public class JwtConfig {
    /**
     * 访问令牌（Access Token）签名密钥
     */
    @Value("${jwt.access-secret:default-access-secret-key-change-in-production}")
    private String accessSecret;

    /**
     * 刷新令牌（Refresh Token）签名密钥
     */
    @Value("${jwt.refresh-secret:default-refresh-secret-key-change-in-production}")
    private String refreshSecret;

    /**
     * JWT签发者标识，必须与令牌中的iss声明匹配
     */
    @Value("${jwt.issuer:system}")
    private String issuer;

    /**
     * JWT受众标识，必须与令牌中的aud声明匹配
     */
    @Value("${jwt.audience}")
    private String audience;

    /**
     * JWT签名密匙最小长度
     */
    @Value("${jwt.min-length:32}")
    private int minLength;

    /**
     * JWT访问令牌有效期（毫秒），默认2小时
     */
    @Value("${jwt.expire:7200000}")
    private long expire;

    /**
     * 刷新令牌有效期（毫秒），默认7天
     */
    @Value("${jwt.refresh-expire:604800000}")
    private long refreshExpire;

    /**
     * @return 用于短期JWT签名令牌的SecretKey
     */
    @Bean
    public SecretKey accessTokenKey() {
        validateSecret(accessSecret, "Access Token Secret");
        return Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @return 用于刷新令牌（Refresh Token）签名的 SecretKey
     */
    @Bean
    public SecretKey refreshTokenKey() {
        validateSecret(refreshSecret, "Refresh Token Secret");
        return Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
    }

    private void validateSecret(String secret, String name) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < minLength) {
            throw new IllegalStateException(
                name + " 长度不足，至少需要 " + minLength + " 字节"
            );
        }
    }
}
