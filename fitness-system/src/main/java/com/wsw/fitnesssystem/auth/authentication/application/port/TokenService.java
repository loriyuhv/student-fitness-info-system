package com.wsw.fitnesssystem.auth.authentication.application.port;

import com.wsw.fitnesssystem.auth.authentication.application.dto.TokenPair;
import com.wsw.fitnesssystem.auth.authentication.infrastructure.token.model.RefreshTokenClaims;
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
