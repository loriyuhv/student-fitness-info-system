package com.wsw.fitnesssystem.iam.authentication.infrastructure.security.encrypt;

import com.wsw.fitnesssystem.iam.authentication.domain.port.PasswordEncryptorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * PasswordEncoder → PasswordEncryptorPort 适配器
 * 使用 SpringSecurity 提供的标准实现
 *
 * @author loriyuhv
 * @version 1.0 2026/1/19 14:38
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class BCryptPasswordEncoderAdapter implements PasswordEncryptorPort {
    /**
     * 使用SpringSecurity的加密技术
     */
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
