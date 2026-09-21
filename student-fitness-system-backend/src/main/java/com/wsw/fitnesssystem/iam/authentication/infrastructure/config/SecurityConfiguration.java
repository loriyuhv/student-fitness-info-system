package com.wsw.fitnesssystem.iam.authentication.infrastructure.config;

import com.wsw.fitnesssystem.iam.authentication.infrastructure.security.filter.JwtAuthenticationFilter;
import com.wsw.fitnesssystem.iam.authentication.infrastructure.security.handler.JwtAccessDeniedHandler;
import com.wsw.fitnesssystem.iam.authentication.infrastructure.security.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置类
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>配置无状态 JWT 认证的过滤器链</li>
 *   <li>注册密码加密器（BCrypt）与 AuthenticationManager</li>
 *   <li>统一认证失败（401）与授权失败（403）的异常处理器</li>
 * </ul>
 *
 * <p><b>配置来源：</b>
 * 放行清单与 BCrypt 强度来自 {@link AuthSecurityProperties}（前缀 {@code iam.authn.security}）。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/13 20:50
 * @since 1.0
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final AuthSecurityProperties authSecurityProperties;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /***
     * 请求的安全过滤规则
     *
     * <p>核心能力：
     * <ol>
     *   <li>关闭 CSRF（无状态 JWT，不存在 CSRF 攻击面）</li>
     *   <li>不使用 HTTP Session（STATELESS）</li>
     *   <li>认证失败 / 授权失败走自定义 Handler，输出统一响应格式</li>
     *   <li>放行清单走 {@link AuthSecurityProperties#getPermitAllPatterns()}</li>
     *   <li>JWT 过滤器挂在 {@link UsernamePasswordAuthenticationFilter} 之前</li>
     * </ol></p>
     *
     * @param http HttpSecurity 构建器
     * @return 构建完成的 {@link SecurityFilterChain}
     * @throws Exception 构建异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. 前后端分离：关闭 CSRF（因为使用JWT，无状态）
            .csrf(AbstractHttpConfigurer::disable)
            // 2. 不使用 Session（因为无状态）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 3. 异常处理（未登录401 / 无权限403）
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 认证失败（401）
                .accessDeniedHandler(jwtAccessDeniedHandler)            // 授权失败（403）
            )
            // 4. 接口权限规则
            .authorizeHttpRequests(auth -> auth
                    // 放行登录等认证接口
                    .requestMatchers(authSecurityProperties.getPermitAllPatterns()
                        .toArray(new String[0])).permitAll()
                    // 其余全部需要认证
                    .anyRequest().authenticated()
                    // 所有接口都放行（不需要鉴权）
                    // .anyRequest().permitAll()
            );
        http
            // 5. JWT 过滤器放在 UsernamePasswordAuthenticationFilter 前
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    /**
     * 密码加密器（BCrypt）
     * <p>strength: 加密强度（4-31），值越大越安全但耗时越长，默认10</p>
     * <p>强度来自 {@link AuthSecurityProperties#getBcryptStrength()}，默认 10。</p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(authSecurityProperties.getBcryptStrength());
    }

    /**
     * AuthenticationManager（供应用层按需注入）
     */
    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

}
