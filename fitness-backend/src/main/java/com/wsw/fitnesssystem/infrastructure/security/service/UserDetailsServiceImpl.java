package com.wsw.fitnesssystem.infrastructure.security.service;

import com.wsw.fitnesssystem.domain.auth.model.AuthUser;
import com.wsw.fitnesssystem.domain.auth.service.AuthDomainService;
import com.wsw.fitnesssystem.infrastructure.security.model.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:39
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AuthDomainService authDomainService;

    /**
     * Spring Security 认证入口
     *
     * @param username 登录账号（学号 / 工号）
     * @return UserDetails
     * @throws UsernameNotFoundException 用户不存在 / 状态异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        AuthUser authUser = authDomainService.loadByUsername(username);
        return new SecurityUser(authUser);
    }
}
