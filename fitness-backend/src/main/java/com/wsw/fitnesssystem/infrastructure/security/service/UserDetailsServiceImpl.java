package com.wsw.fitnesssystem.infrastructure.security.service;

import com.wsw.fitnesssystem.domain.user.User;
import com.wsw.fitnesssystem.domain.user.UserRepository;
import com.wsw.fitnesssystem.infrastructure.security.model.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:39
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Spring Security 认证入口
     *
     * @param username 登录账号（学号 / 工号）
     * @return UserDetails
     * @throws UsernameNotFoundException 用户不存在 / 状态异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        // 1. 查询用户（排除已删除的）
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                log.warn("用户不存在 username={}", username);
                return new UsernameNotFoundException("用户不存在");
            });

        // 2. 校验用户状态
        if (!user.isEnabled()) {
            log.warn("登录失败：用户已禁用 - userId: {}, username: {}",
                user.getUserId(), username);
            // TODO
            // throw new DisabledException("用户已被禁用，请联系管理员");
        }

        // 3. 查询权限（一次查完）
        Set<String> perms = userRepository.findPermissions(user.getUserId());

        return new LoginUser(user, perms);
    }
}
