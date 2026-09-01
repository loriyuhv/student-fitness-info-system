package com.wsw.fitnesssystem.user.infrastructure.encrypt;

import com.wsw.fitnesssystem.user.domain.port.PasswordEncryptorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/1 18:52
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class LocalPasswordEncryptor implements PasswordEncryptorPort {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
