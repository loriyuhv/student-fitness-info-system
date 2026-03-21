package com.wsw.fitnesssystem.auth.application.service.impl;

import com.wsw.fitnesssystem.auth.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.domain.model.TokenPair;
import com.wsw.fitnesssystem.auth.domain.port.TokenGenerator;
import com.wsw.fitnesssystem.auth.application.service.TokenApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 14:01
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class TokenApplicationServiceImpl implements TokenApplicationService {
    private final TokenGenerator tokenGenerator;

    @Override
    public TokenPair generate(AuthUser user) {
        String accessTokenId = UUID.randomUUID().toString();
        String refreshTokenId = UUID.randomUUID().toString();

        return tokenGenerator.generate(
            user, accessTokenId, refreshTokenId
        );
    }
}
