package com.wsw.fitnesssystem.iam.authentication.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证安全配置属性（基础设施层配置）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>绑定 {@code iam.authn.security.*} 下的安全相关配置项</li>
 *   <li>承载 Spring Security 过滤器链所需的放行清单与密码加密强度</li>
 * </ul>
 *
 * <p><b>配置示例：</b>
 * <pre>{@code
 * iam:
 *   authn:
 *     security:
 *       permit-all-patterns:
 *         - /authn/login
 *         - /authn/refresh
 *       bcrypt-strength: 10
 * }</pre></p>
 *
 * <p><b>默认值兜底策略：</b>
 * <ul>
 *   <li>{@link #permitAllPatterns} 默认空列表，避免 null 导致 NPE；</li>
 *   <li>{@link #bcryptStrength} 默认 10，与 Spring Security 官方默认值一致。</li>
 * </ul></p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/24 11:08
 * @since 1.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "iam.authn.security")
public class AuthSecurityProperties {

    /**
     * 无需认证即可放行的接口路径（servletPath，不带 context-path 前缀）
     * <p>用于 {@code SecurityFilterChain#authorizeHttpRequests} 的 {@code permitAll()} 匹配。</p>
     */
    private List<String> permitAllPatterns = new ArrayList<>();

    /**
     * BCrypt 加密强度（4 ~ 31）
     * <p>值越大越安全但耗时越长，10 为 Spring Security 默认值，兼顾安全与性能。</p>
     */
    private Integer bcryptStrength = 10;

}
