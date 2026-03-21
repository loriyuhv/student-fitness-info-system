package com.wsw.fitnesssystem.auth.application.authentication;

import com.wsw.fitnesssystem.auth.application.authentication.command.LoginCommand;
import com.wsw.fitnesssystem.auth.application.authentication.dto.LoginResponse;
import com.wsw.fitnesssystem.auth.application.authorization.AuthorizationApplicationService;
import com.wsw.fitnesssystem.auth.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.domain.model.TokenPair;
import com.wsw.fitnesssystem.auth.domain.port.SessionRepository;
import com.wsw.fitnesssystem.auth.domain.port.TokenGenerator;
import com.wsw.fitnesssystem.auth.domain.service.AuthDomainService;
import com.wsw.fitnesssystem.auth.domain.service.SessionDomainService;
import com.wsw.fitnesssystem.auth.infrastructure.audit.service.LoginAuditService;
import com.wsw.fitnesssystem.auth.infrastructure.config.SessionProperties;
import com.wsw.fitnesssystem.auth.infrastructure.jwt.model.JwtUserClaims;
import com.wsw.fitnesssystem.auth.infrastructure.jwt.service.JwtTokenService;
import com.wsw.fitnesssystem.auth.infrastructure.security.service.LoginFailLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 用户认证用例
 * 职责边界（非常重要）：
 * 1. 只负责【登录 / 登出 / 踢人】
 * 2. 只操作 Redis 登录态
 * 3. 不感知 Filter / SecurityContext
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:16
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final AuthDomainService authDomainService;
    private final LoginAuditService loginAuditService;
    private final SessionDomainService sessionDomainService;
    private final LoginFailLimitService loginFailLimitService;
    private final TokenGenerator tokenGenerator;
    private final SessionRepository sessionRepository;
    private final JwtTokenService jwtTokenService;
    private final SessionProperties sessionProperties;
    private final AuthorizationApplicationService authorizationApplicationService;

    public LoginResponse login(LoginCommand loginCommand) {
        // 1. 失败次数校验（策略）
        // loginFailLimitService.check(request.getUsername());

        // 2. 用户认证
        AuthUser user = authDomainService.login(
            loginCommand.getUsername(),
            loginCommand.getPassword()
        );

        // 3. 限制多端登录
        sessionDomainService.limitSessions(
            user.getCampusId(), user.getUserId(), 3
        );

        // 4. 生成JWT
        // 4.1 生成 tokenId
        String accessTokenId = UUID.randomUUID().toString();
        String refreshTokenId = UUID.randomUUID().toString();

        // 4.2 生成 Token对
        TokenPair tokenPair = tokenGenerator.generate(
            user, accessTokenId, refreshTokenId
        );

        // 6. 保存 Redis 会话
        sessionRepository.saveSession(
            user.getCampusId(), user.getUserId(),
            accessTokenId, refreshTokenId
        );

        // 6. 授权（一次性，内部自动读/写缓存） 优化：懒加载
        authorizationApplicationService.authorize(user);

        // 7. 记录成功审计
        loginAuditService.loginSuccess(
            user.getUserId(),
            loginCommand,
            tokenPair
        );

        // 8. 返回结果
        return LoginResponse.builder()
            .tokenId(tokenPair.getTokenId())
            .accessToken(tokenPair.getAccessToken())
            .refreshToken(tokenPair.getRefreshToken())
            .expiresIn(tokenPair.getExpire())
            .build();
    }

    /***
     * 用户登出
     * @param accessToken JWT令牌
     */
    public void logout(String accessToken) {
        JwtUserClaims claims = jwtTokenService.parseAccessToken(accessToken);
        String tokenId = claims.getTokenId();
        Long campusId = claims.getCampusId();
        Long userId = claims.getUserId();

        // 1. 将当前 accessToken 加入黑名单
        sessionRepository.addToBlacklist(
            tokenId,
            sessionProperties.getAccessTokenExpireMinutes() * 60
        );

        // 2. 从 ZSET 中删除（会话下线）
        sessionRepository.removeSession(campusId, userId, tokenId);

        // 3. 记录登出审计
        loginAuditService.logout(userId, tokenId);
    }

    /***
     * 踢人操作（可选）
     *
     * @param accessToken JWT令牌
     */
    public void kick(String accessToken) {
        JwtUserClaims claims = jwtTokenService.parseAccessToken(accessToken);
        String tokenId = claims.getTokenId();
        Long campusId = claims.getCampusId();
        Long userId = claims.getUserId();

        // 1. 将当前 accessToken 加入黑名单
        sessionRepository.addToBlacklist(
            tokenId,
            sessionProperties.getAccessTokenExpireMinutes() * 60
        );

        // 2. 从 ZSET 中删除（会话下线）
        sessionRepository.removeSession(campusId, userId, tokenId);

        loginAuditService.kick(userId, tokenId);
    }

}
