package com.wsw.fitnesssystem.auth.authentication.infrastructure.token.service;

import com.wsw.fitnesssystem.auth.authentication.application.dto.TokenPair;
import com.wsw.fitnesssystem.auth.authentication.application.port.TokenService;
import com.wsw.fitnesssystem.auth.authentication.infrastructure.config.JwtConfig;
import com.wsw.fitnesssystem.auth.authentication.infrastructure.token.model.AccessTokenClaims;
import com.wsw.fitnesssystem.auth.authentication.infrastructure.token.model.RefreshTokenClaims;
import com.wsw.fitnesssystem.auth.authentication.infrastructure.token.model.TokenPrincipal;
import com.wsw.fitnesssystem.auth.authentication.infrastructure.token.parser.JwtTokenParser;
import com.wsw.fitnesssystem.auth.authentication.infrastructure.token.provider.JwtTokenProvider;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/***
 * JWT Token 业务服务
 *
 * <p>
 * 核心职责：
 * <ul>
 *   <li>登录成功后签发 Access / Refresh Token</li>
 *   <li>刷新 Access Token</li>
 *   <li>撤销 Token（退出登录 / 被踢下线）</li>
 *   <li>和 Redis 协作管理 Token 生命周期</li>
 * </ul>
 *
 * <p>
 * 不负责：
 * <ul>
 *   <li>JWT 解析（交给 JwtTokenParser）</li>
 *   <li>JWT 生成细节（交给 JwtTokenProvider）</li>
 *   <li>用户认证（账号密码校验在 AuthService）</li>
 *   <li>权限加载（Security Filter / Authorization 再做）</li>
 * </ul>
 *
 * <p>
 * 协作组件：
 * <ul>
 *   <li>{@link JwtTokenProvider}：只负责生成 JWT</li>
 *   <li>{@link JwtTokenParser}：只负责解析 / 校验 JWT</li>
 *   <li>Redis：管理 Token 状态</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/15 1:30
 * @since 1.0
 */
@Slf4j
@Service
@Getter
@RequiredArgsConstructor
public class JwtTokenService implements TokenService {
    private final JwtConfig jwtConfig;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenParser jwtTokenParser;

    @Override
    public TokenPair generate(
            Operator operator, String deviceId, Long tokenVersion,
            String accessTokenId, String refreshTokenId) {
        String accessToken = generateAccessToken(
                operator,
                tokenVersion,
                accessTokenId
        );
        String refreshToken = generateRefreshToken(
                operator,
                deviceId,
                tokenVersion,
                refreshTokenId);

        return TokenPair.builder()
            .accessTokenId(accessTokenId)
            .refreshTokenId(refreshTokenId)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .accessTokenExpiresIn(jwtConfig.getExpire() / 1000L)
            .refreshTokenExpiresIn(jwtConfig.getRefreshExpire() / 1000L)
            .build();
    }

    public AccessTokenClaims parseAccessToken(String accessToken) {
        return jwtTokenParser.parseAccessToken(accessToken);
    }

    public RefreshTokenClaims parseRefreshToken(String refreshToken) {
        return jwtTokenParser.parseRefreshToken(refreshToken);
    }

    private String generateAccessToken(
            Operator operator, Long tokenVersion, String accessTokenId) {

        return jwtTokenProvider.generateAccessToken(
            TokenPrincipal.builder()
                    .userId(operator.userId())
                    .campusId(operator.campusId())
                    .username(operator.username())
                    .userType(operator.userType())
                    .tokenVersion(tokenVersion)
                    .build(),
                accessTokenId);
    }

    private String generateRefreshToken(
            Operator operator, String deviceId, Long tokenVersion,
            String refreshTokenId) {

        return jwtTokenProvider.generateRefreshToken(
            TokenPrincipal.builder()
                    .campusId(operator.campusId())
                    .userId(operator.userId())
                    .deviceId(deviceId)
                    .tokenVersion(tokenVersion)
                    .build(),
                refreshTokenId);
    }
}
