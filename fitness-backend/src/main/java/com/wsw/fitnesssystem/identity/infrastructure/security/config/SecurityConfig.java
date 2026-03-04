package com.wsw.fitnesssystem.identity.infrastructure.security.config;

import com.wsw.fitnesssystem.identity.infrastructure.security.filter.JwtAuthenticationFilter;
import com.wsw.fitnesssystem.identity.infrastructure.security.handler.JwtAccessDeniedHandler;
import com.wsw.fitnesssystem.identity.infrastructure.security.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置类
 *
 * @author loriyuhv
 * @version 1.0 2026/1/13 20:50
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /***
     * 请求的安全过滤规则。核心功能如下：
     * 1.关闭CSRF防护 csrf(csrf -> csrf.disable())
     * 2.放行所有接口 auth.anyRequest().permitAll()
     *
     * @param http 请求信息
     * @return 经过校验的请求
     * @throws Exception 请求权限等异常
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
            // 3. 异常处理（未登录 / 无权限）
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 认证失败（401）
                .accessDeniedHandler(jwtAccessDeniedHandler)            // 授权失败（403）
            )
            // 4. 接口权限规则
            .authorizeHttpRequests(auth -> auth
                    // 放行登录等认证接口
                    .requestMatchers("/auth/login", "/auth/refresh").permitAll()
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
     * 密码加密器 Bean
     *  strength: 加密强度（4-31），值越大越安全但耗时越长，默认10
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * AuthenticationManager（给 AuthApplicationService 用）
     */
    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
