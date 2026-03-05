package com.wsw.fitnesssystem.auth.application.authentication;

import com.wsw.fitnesssystem.auth.application.authentication.command.LoginCommand;
import com.wsw.fitnesssystem.auth.application.authentication.dto.LoginResult;
import com.wsw.fitnesssystem.auth.application.authorization.AuthorizationApplicationService;
import com.wsw.fitnesssystem.auth.application.authorization.UserAuthorization;
import com.wsw.fitnesssystem.auth.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.domain.service.AuthDomainService;
import com.wsw.fitnesssystem.auth.infrastructure.audit.service.LoginAuditService;
import com.wsw.fitnesssystem.auth.infrastructure.security.authorization.AuthorizationCacheService;
import com.wsw.fitnesssystem.auth.infrastructure.security.service.LoginFailLimitService;
import com.wsw.fitnesssystem.auth.infrastructure.session.LoginSession;
import com.wsw.fitnesssystem.auth.infrastructure.session.LoginSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final LoginSessionService loginSessionService;
    private final LoginAuditService loginAuditService;
    private final LoginFailLimitService loginFailLimitService;
    private final AuthorizationApplicationService authorizationApplicationService;
    private final AuthorizationCacheService authorizationCacheService;

    public LoginResult login(LoginCommand loginCommand) {
        // 1. 失败次数校验（策略）
        // loginFailLimitService.check(request.getUsername());

        // 2. 核心业务登录
        AuthUser authUser = authDomainService.login(
            loginCommand.getUsername(),
            loginCommand.getPassword()
        );

        // 3. 授权（一次性）
        UserAuthorization authorization =
            authorizationApplicationService.authorize(authUser);

        // 4. 创建登录会话（生成 token + 写 Redis）
        LoginSession session = loginSessionService.createSession(authUser);

        // 4. 缓存权限
        authorizationCacheService.cache(authorization, session.getTokenId());

        // 5. 记录成功审计
        loginAuditService.loginSuccess(
            authUser.getUserId(),
            loginCommand,
            session
        );

        // 5. 返回结果
        return LoginResult.builder()
            .tokenId(session.getTokenId())
            .accessToken(session.getAccessToken())
            .refreshToken(session.getRefreshToken())
            .expiresIn(session.getExpire())
            .build();
    }
}
