package com.wsw.fitnesssystem.infrastructure.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置类
 *
 * @author loriyuhv
 * @version 1.0 2026/1/13 20:50
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
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
            // 2. 接口权限规则
            .authorizeHttpRequests(auth -> auth
                // 所有接口都放行（不需要鉴权）
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
