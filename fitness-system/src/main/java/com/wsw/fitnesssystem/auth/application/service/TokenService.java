package com.wsw.fitnesssystem.auth.application.service;

import com.wsw.fitnesssystem.auth.application.dto.TokenPair;
import com.wsw.fitnesssystem.auth.infrastructure.token.model.RefreshTokenClaims;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/22 16:48
 * @since 1.0
 */
public interface TokenService {
    TokenPair generate(
            Operator operator, String deviceId, Long tokenVersion,
            String accessTokenId, String refreshTokenId);

    RefreshTokenClaims parseRefreshToken(String refreshToken);
}
