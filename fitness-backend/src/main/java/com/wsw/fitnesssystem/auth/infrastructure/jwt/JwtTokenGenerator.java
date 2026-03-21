package com.wsw.fitnesssystem.auth.infrastructure.jwt;

import com.wsw.fitnesssystem.auth.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.domain.model.TokenPair;
import com.wsw.fitnesssystem.auth.domain.port.TokenGenerator;
import com.wsw.fitnesssystem.auth.infrastructure.jwt.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 10:13
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class JwtTokenGenerator implements TokenGenerator {
    private final JwtTokenService jwtTokenService;

    @Override
    public TokenPair generate(AuthUser user, String accessTokenId, String refreshTokenId) {
        // 1. 生成JWT AccessToken
        String accessToken = jwtTokenService.generateAccessToken(
            user.getCampusId(),
            user.getUserId(),
            user.getUsername(),
            accessTokenId
        );

        // 2. 生成JWT RefreshToken
        String refreshToken = jwtTokenService.generateRefreshToken(
            user.getCampusId(),
            user.getUserId(),
            refreshTokenId
        );

        // 3. 返回Token对
        return TokenPair.builder()
            .tokenId(accessTokenId)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expire(jwtTokenService.getJwtConfig().getExpire())
            .build();
    }
}
