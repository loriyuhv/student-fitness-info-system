package com.wsw.fitnesssystem.auth.infrastructure.security.service;

import com.wsw.fitnesssystem.auth.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.domain.repository.AuthUserRepository;
import com.wsw.fitnesssystem.auth.infrastructure.security.model.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:39
 * @since 1.0
 */
@Slf4j
// @Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AuthUserRepository authUserRepository;

    /**
     * Spring Security 认证入口
     *
     * @param username 登录账号（学号 / 工号）
     * @return UserDetails
     * @throws UsernameNotFoundException 用户不存在 / 状态异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) {

        AuthUser user = authUserRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        return SecurityUser.from(user);
    }
}
