package com.wsw.fitnesssystem.auth.application.authentication;

import com.wsw.fitnesssystem.auth.application.authentication.command.LoginCommand;
import com.wsw.fitnesssystem.auth.application.authentication.dto.LoginResponse;
import com.wsw.fitnesssystem.auth.application.service.LoginSuccessProcessor;
import com.wsw.fitnesssystem.auth.application.service.RiskControlService;
import com.wsw.fitnesssystem.auth.application.service.TokenService;
import com.wsw.fitnesssystem.auth.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.application.dto.TokenPair;
import com.wsw.fitnesssystem.auth.domain.port.SessionRepository;
import com.wsw.fitnesssystem.auth.domain.service.AuthDomainService;
import com.wsw.fitnesssystem.auth.infrastructure.audit.LoginAuditService;
import com.wsw.fitnesssystem.auth.infrastructure.config.SessionProperties;
import com.wsw.fitnesssystem.auth.infrastructure.security.model.JwtUserPrincipal;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
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

    private final RiskControlService riskControlService;
    private final AuthDomainService authDomainService;
    private final TokenService tokenService;
    private final LoginSuccessProcessor loginSuccessProcessor;
    private final LoginAuditService loginAuditService;
    private final SessionRepository sessionRepository;
    private final SessionProperties sessionProperties;

    /**
     * 用户登录流程（应用服务入口）
     *
     * <p>该方法负责用户登录的完整应用层流程，遵循分阶段编排：
     * <ol>
     *     <li>风控前置检查：校验账号是否被锁定及失败次数限制 {@link RiskControlService#preCheck(String)}</li>
     *     <li>用户认证：调用领域服务验证用户名和密码 {@link #authenticate(LoginCommand)}</li>
     *     <li>生成 Token：生成 Access Token 与 Refresh Token {@link com.wsw.fitnesssystem.auth.application.service.TokenService}</li>
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
        String accessTokenId = UUID.randomUUID().toString();
        String refreshTokenId = UUID.randomUUID().toString();
        TokenPair tokenPair = tokenService.generate(
            user.getUserId(),
            user.getCampusId(),
            user.getUsername(),
            cmd.getDeviceId(),
            accessTokenId,
            refreshTokenId
        );

        // 4. 登录成功后处理
        loginSuccessProcessor.process(user, cmd, tokenPair);

        // 5. 返回
        return buildResponse(tokenPair);
    }

    /***
     * 用户登出
     */
    public void logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        String accessTokenId = principal.accessTokenId();
        Long campusId = principal.campusId();
        Long userId = principal.userId();

        // 1. 将当前 accessToken 加入黑名单
        sessionRepository.addToBlacklist(
            accessTokenId,
            sessionProperties.getAccessTokenExpireMinutes() * 60
        );

        // 2. 从 ZSET 中删除（会话下线）
        sessionRepository.removeSession(campusId, userId, accessTokenId);

        // 3. 记录登出审计
        loginAuditService.logout(userId, accessTokenId);
    }

    /**
     * 踢人操作（管理员使用）
     *
     * @param campusId 校区ID
     * @param userId   用户ID
     */
    public void kick(Long campusId, Long userId) {
        // 1. 校验用户是否存在
        if (!authDomainService.userExists(campusId, userId)) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }

        // 1. 获取用户所有在线 Access Token ID
        Set<String> tokenIds = sessionRepository.getAllSessions(campusId, userId);

        if (tokenIds == null || tokenIds.isEmpty()) {
            log.info("User {} campus {} has no online session to kick.", userId, campusId);
            return;
        }

        for (String tokenId : tokenIds) {
            // 2. 从在线会话删除，加入黑名单
            sessionRepository.removeSession(campusId, userId, tokenId);

            // 3. 记录审计
            loginAuditService.kick(userId, tokenId);
        }

        log.info("Kicked {} sessions for user {} campus {}", tokenIds.size(), userId, campusId);
    }

    public TokenPair refresh(String refreshToken) {

        // Claims claims = jwtService.parse(refreshToken);
        //
        // Long userId = claims.get("userId", Long.class);
        // Long campusId = claims.get("campusId", Long.class);
        // String deviceId = claims.get("deviceId", String.class);
        // String jti = claims.getId();
        //
        // // 校验 Redis
        // String key = buildKey(campusId, userId, deviceId);
        // String storedJti = redis.get(key);
        //
        // if (!jti.equals(storedJti)) {
        //     throw new BizException("RefreshToken 失效");
        // }
        //
        // // 生成新 Token
        // TokenPair newToken = tokenService.generate(userId, campusId, deviceId);
        //
        // // 覆盖 Redis（轮换）
        // redis.set(key, newToken.getRefreshJti(), 7, DAYS);
        return null;
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
            .accessToken(tokenPair.getAccessToken())
            .refreshToken(tokenPair.getRefreshToken())
            .expiresIn(tokenPair.getAccessTokenExpiresIn())
            .build();
    }

}
