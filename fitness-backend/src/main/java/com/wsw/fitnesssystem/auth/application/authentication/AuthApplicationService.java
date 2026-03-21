package com.wsw.fitnesssystem.auth.application.authentication;

import com.wsw.fitnesssystem.auth.application.authentication.command.LoginCommand;
import com.wsw.fitnesssystem.auth.application.authentication.dto.LoginResponse;
import com.wsw.fitnesssystem.auth.application.service.LoginSuccessProcessor;
import com.wsw.fitnesssystem.auth.application.service.RiskControlService;
import com.wsw.fitnesssystem.auth.application.service.TokenApplicationService;
import com.wsw.fitnesssystem.auth.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.domain.model.TokenPair;
import com.wsw.fitnesssystem.auth.domain.port.SessionRepository;
import com.wsw.fitnesssystem.auth.domain.service.AuthDomainService;
import com.wsw.fitnesssystem.auth.infrastructure.audit.service.LoginAuditService;
import com.wsw.fitnesssystem.auth.infrastructure.config.SessionProperties;
import com.wsw.fitnesssystem.auth.infrastructure.jwt.model.JwtUserClaims;
import com.wsw.fitnesssystem.auth.infrastructure.jwt.service.JwtTokenService;
import com.wsw.fitnesssystem.shared.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final RiskControlService riskControlService;
    private final AuthDomainService authDomainService;
    private final TokenApplicationService tokenApplicationService;
    private final LoginSuccessProcessor loginSuccessProcessor;

    private final LoginAuditService loginAuditService;
    private final SessionRepository sessionRepository;
    private final JwtTokenService jwtTokenService;
    private final SessionProperties sessionProperties;

    /**
     * 用户登录流程（应用服务入口）
     *
     * <p>该方法负责用户登录的完整应用层流程，遵循分阶段编排：
     * <ol>
     *     <li>风控前置检查：校验账号是否被锁定及失败次数限制 {@link RiskControlService#preCheck(String)}</li>
     *     <li>用户认证：调用领域服务验证用户名和密码 {@link #authenticate(LoginCommand)}</li>
     *     <li>生成 Token：生成 Access Token 与 Refresh Token {@link com.wsw.fitnesssystem.auth.application.service.TokenApplicationService}</li>
     *     <li>登录成功后处理：多端限制、会话持久化、授权缓存、审计 {@link com.wsw.fitnesssystem.auth.application.service.LoginSuccessProcessor}</li>
     *     <li>构建返回结果：封装登录响应 {@link #buildResponse(TokenPair)}</li>
     * </ol>
     *
     * <p>方法特点：
     * <ul>
     *     <li>应用服务只负责流程编排，具体逻辑委托给各子服务</li>
     *     <li>保证流程清晰、职责单一、易扩展</li>
     *     <li>异常处理由子服务统一处理，例如认证失败会触发风控策略</li>
     * </ul>
     *
     * @param cmd 登录请求命令对象 {@link LoginCommand}，包含用户名和密码
     * @return {@link LoginResponse} 登录响应对象，包含 Access Token、Refresh Token、Token ID 及过期时间
     * @throws BizException 当用户名或密码错误，或其他业务异常时抛出
     */
    public LoginResponse login(LoginCommand cmd) {
        /* 1. 风控前置检查 */
        riskControlService.preCheck(cmd.getUsername());

        /* 2. 认证 */
        AuthUser user = authenticate(cmd);

        // 3. 生成 Token
        TokenPair tokenPair = tokenApplicationService.generate(user);

        // 4. 登录成功后处理
        loginSuccessProcessor.process(user, cmd, tokenPair);

        // 5. 返回
        return buildResponse(tokenPair);
    }

    /***
     * 用户登出
     * @param accessToken JWT令牌
     */
    public void logout(String accessToken) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

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

    /**
     * 执行用户认证
     *
     * <p>调用领域服务 {@link com.wsw.fitnesssystem.auth.domain.service.AuthDomainService} 完成用户认证。
     * 认证成功返回 {@link AuthUser}；认证失败则通过 {@link RiskControlService#onFail(String)} 统一处理失败计数和锁定策略，
     * 然后抛出异常。
     *
     * <p>职责说明：
     * <ul>
     *     <li>调用领域服务执行登录认证</li>
     *     <li>捕获业务异常，统一处理登录失败策略</li>
     *     <li>返回认证成功的用户信息供后续流程使用</li>
     * </ul>
     *
     * @param cmd 登录命令对象 {@link LoginCommand}，包含用户名和密码
     * @return {@link AuthUser} 登录成功的用户信息
     * @throws BizException 当认证失败时抛出，用于上层捕获和流程控制
     */
    private AuthUser authenticate(LoginCommand cmd) {
        try {
            return authDomainService.login(
                cmd.getUsername(),
                cmd.getPassword()
            );
        } catch (BizException ex) {
            // 登录失败处理（统一收口）
            int failCount = riskControlService.onFail(cmd.getUsername());
            // 登录失败审计
            loginAuditService.loginFail(
                cmd.getUsername(),
                cmd.getIp(),
                cmd.getDeviceType(),
                cmd.getUserAgent(),
                ex.getMessage(),  // 失败原因
                failCount,
                failCount >= 5    // 是否达到锁定阈值
            );

            throw ex;
        }
    }

    /**
     * 构建登录响应对象
     *
     * <p>将生成的 {@link TokenPair} 转换为应用层的 {@link LoginResponse} 返回给客户端。
     * 负责封装：
     * <ul>
     *     <li>Token ID</li>
     *     <li>Access Token</li>
     *     <li>Refresh Token</li>
     *     <li>过期时间（秒）</li>
     * </ul>
     *
     * @param tokenPair 登录成功生成的 Token 对象 {@link TokenPair}
     * @return {@link LoginResponse} 返回给客户端的登录响应
     */
    private LoginResponse buildResponse(TokenPair tokenPair) {
        return LoginResponse.builder()
            .tokenId(tokenPair.getAccessTokenId())
            .accessToken(tokenPair.getAccessToken())
            .refreshToken(tokenPair.getRefreshToken())
            .expiresIn(tokenPair.getExpire())
            .build();
    }

}
