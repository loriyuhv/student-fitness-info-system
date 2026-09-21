package com.wsw.fitnesssystem.iam.authentication.infrastructure.config;

import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT 签名密钥配置
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>根据 {@link JwtProperties} 中的密钥字符串生成 {@link SecretKey} Bean</li>
 *   <li>在启动阶段校验密钥长度，不满足 {@link JwtProperties#getMinLength()} 时直接拒绝启动</li>
 *   <li>Access / Refresh 使用两把独立密钥，避免令牌类型混淆的安全风险</li>
 * </ul>
 *
 * <p><b>与 JwtProperties 的分工：</b>
 * <ul>
 *   <li>{@code JwtProperties}：只承载配置数据，不含任何 Bean 生产逻辑</li>
 *   <li>{@code JwtKeyConfiguration}：只负责把配置转换为可注入的技术组件 {@link SecretKey}</li>
 * </ul>
 *
 * <p><b>fail-fast 设计：</b>
 * 密钥缺失或长度不足时不生成 Bean，应用启动直接失败，
 * 避免运行时才暴露"密钥为空导致所有令牌无效"的隐蔽故障。</p>
 *
 * <p><b>依赖注入：</b>
 * 通过 Bean 名称区分两把密钥；注入方需使用 {@code @Qualifier} 或按参数名匹配：
 * <pre>{@code
 * private final SecretKey accessTokenKey;   // 按 Bean 名称自动匹配
 * private final SecretKey refreshTokenKey;
 * }</pre></p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21 11:05
 * @since 1.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class JwtKeyConfiguration {

    private final JwtProperties jwtProperties;

    /**
     * 访问令牌签名密钥 Bean
     *
     * @return 基于 HMAC-SHA256 的 {@link SecretKey}
     * @throws IllegalStateException 密钥缺失或长度不足
     */
    @Bean
    public SecretKey accessTokenKey() {
        validateSecret(jwtProperties.getAccessSecret(), "Access Token Secret");
        log.info("JWT access token key initialized, minLength={}", jwtProperties.getMinLength());
        return Keys.hmacShaKeyFor(
            jwtProperties.getAccessSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * 刷新令牌签名密钥 Bean
     *
     * @return 基于 HMAC-SHA256 的 {@link SecretKey}
     * @throws IllegalStateException 密钥缺失或长度不足
     */
    @Bean
    public SecretKey refreshTokenKey() {
        validateSecret(jwtProperties.getRefreshSecret(), "Refresh Token Secret");
        log.info("JWT refresh token key initialized, minLength={}", jwtProperties.getMinLength());
        return Keys.hmacShaKeyFor(
            jwtProperties.getRefreshSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * 校验密钥长度是否满足最小要求
     *
     * @param secret 密钥字符串
     * @param name   密钥名称（用于异常提示）
     * @throws IllegalStateException 密钥为空或长度不足
     */
    private void validateSecret(String secret, String name) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < jwtProperties.getMinLength()) {
            throw new IllegalStateException(
                name + " 长度不足，至少需要 " + jwtProperties.getMinLength() + " 字节"
            );
        }
    }

}
