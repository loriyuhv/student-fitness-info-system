package com.wsw.fitnesssystem.auth.infrastructure.security.password;

import com.wsw.fitnesssystem.auth.domain.port.PasswordEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * PasswordEncoder → PasswordEncryptor 适配器
 * 使用 SpringSecurity 提供的标准实现
 *
 * @author loriyuhv
 * @version 1.0 2026/1/19 14:38
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class PasswordEncoderAdapter implements PasswordEncryptor {
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
