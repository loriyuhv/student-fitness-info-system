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
    @Value("${auth.jwt.access-secret}")
    private String accessSecret;

    /**
     * 刷新令牌（Refresh Token）签名密钥
     */
    @Value("${auth.jwt.refresh-secret}")
    private String refreshSecret;

    /**
     * JWT签发者标识，必须与令牌中的iss声明匹配
     */
    @Value("${auth.jwt.issuer:system}")
    private String issuer;

    /**
     * JWT受众标识，必须与令牌中的aud声明匹配
     */
    @Value("${auth.jwt.audience}")
    private String audience;

    /**
     * JWT签名密匙最小长度
     */
    @Value("${auth.jwt.min-length:32}")
    private int minLength;

    /**
     * JWT访问令牌有效期（秒），默认15分钟(900秒)
     */
    @Value("${auth.jwt.expire:900}")
    private long expire;

    /**
     * JWT刷新令牌有效期（秒），默认7天(604800秒)
     */
    @Value("${auth.jwt.refresh-expire:604800}")
    private long refreshExpire;

    /**
     * 获取访问令牌过期时间（毫秒）
     * <p>配置值expire单位为秒，内部自动转换为毫秒，用于JWT生成签发过期时间</p>
     *
     * @return 访问令牌过期时间，单位：毫秒
     */
    public long getExpire() {
        return expire * 1000L;
    }

    /**
     * 获取刷新令牌过期时间（毫秒）
     * <p>配置值refreshExpire单位为秒，内部自动转换为毫秒，用于刷新Token过期判断</p>
     *
     * @return 刷新令牌过期时间，单位：毫秒
     */
    public long getRefreshExpire() {
        return refreshExpire * 1000L;
    }

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
