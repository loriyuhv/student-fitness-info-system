package com.wsw.fitnesssystem.auth.authentication.application;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wsw.fitnesssystem.auth.authentication.application.dto.command.LoginCommand;
import com.wsw.fitnesssystem.auth.authentication.application.dto.command.RefreshCommand;
import com.wsw.fitnesssystem.auth.authentication.application.dto.port.AuthUserCredential;
import com.wsw.fitnesssystem.auth.authentication.application.dto.port.RiskCheckResult;
import com.wsw.fitnesssystem.auth.authentication.application.dto.result.LoginResult;
import com.wsw.fitnesssystem.auth.authentication.application.dto.result.RefreshResult;
import com.wsw.fitnesssystem.auth.authentication.application.event.LoginFailureEvent;
import com.wsw.fitnesssystem.auth.authentication.application.event.LoginSuccessEvent;
import com.wsw.fitnesssystem.auth.authentication.application.event.RefreshTokenEvent;
import com.wsw.fitnesssystem.auth.authentication.application.event.SessionTerminatedEvent;
import com.wsw.fitnesssystem.auth.authentication.application.port.*;
import com.wsw.fitnesssystem.auth.authentication.domain.port.PasswordEncryptor;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.LogoutReason;
import com.wsw.fitnesssystem.auth.authentication.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.authentication.application.dto.port.TokenPair;
import com.wsw.fitnesssystem.auth.authentication.application.dto.port.RefreshTokenClaims;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * 用户认证应用服务
 *
 * <p>职责：编排登录、登出、踢人、刷新 Token 的完整流程。
 * <p>依赖：通过端口（Port）解耦外部模块，通过事件发布审计。
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:16
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthAppService {

    // ==================== 端口依赖 ====================

    /** 风控端口 */
    private final RiskPort riskPort;

    /** Token 生成/解析端口 */
    private final TokenPort tokenPort;

    /** 会话管理端口 */
    private final SessionPort sessionPort;

    /** 授权管理端口 */
    private final AuthorizationPort authorizationPort;

    /** 密码加密器（领域端口） */
    private final PasswordEncryptor passwordEncryptor;

    /** 用户认证数据提供者端口 */
    private final AuthUserDataProvider authUserDataProvider;

    /** 事件发布器（用于异步审计） */
    private final ApplicationEventPublisher eventPublisher;

    // ==================== 登录 ====================

    /**
     * 用户登录
     *
     * <p>流程：风控检查 → 认证 → 生成 Token → 后置处理（风控/会话/审计）→ 返回结果
     *
     * @param cmd 登录命令
     * @return 登录结果（Token 对）
     */
    public LoginResult login(LoginCommand cmd) {
        // 1. 风控前置检查
        riskPort.preCheck(cmd.getUsername());

        // 2. 用户认证
        AuthUser user = authenticate(cmd);

        // 3. 生成 Token
        String accessTokenId = UUID.randomUUID().toString();
        String refreshTokenId = UUID.randomUUID().toString();
        long userId = user.getUserId();
        long campusId = user.getCampusId();
        long tokenVersion = sessionPort.getTokenVersion(campusId, userId);

        TokenPair tokenPair = tokenPort.generate(
            campusId, userId, user.getUsername(), user.getUserType(),
            cmd.getDeviceId(), tokenVersion, accessTokenId, refreshTokenId
        );

        // 4. 登录成功 → 后置处理（风控 + 会话 + 审计）
        handleLoginSuccess(
            userId, campusId, user.getUsername(), accessTokenId, refreshTokenId,
            tokenPair.getAccessTokenExpiresIn(), cmd.getDeviceType(), cmd.getUserAgent(), cmd.getIp()
        );

        // 5. 返回
        return buildLoginResult(tokenPair);
    }

    /***
     * 用户登出
     *
     * <p>将 Token 加入黑名单 → 从在线会话中移除 → 发布登出审计事件
     *
     */
    public void logout(Operator operator, String accessTokenId) {
        // 1. 将当前 accessToken 加入黑名单
        sessionPort.addToBlacklist(accessTokenId);

        // 2. 从 ZSET 中删除（会话下线）
        sessionPort.removeSession(operator.campusId(), operator.userId(), accessTokenId);

        // 3. 记录登出审计
        eventPublisher.publishEvent(
            new SessionTerminatedEvent(this, accessTokenId, LogoutReason.LOGOUT));
    }

    // ==================== 踢人 ====================

    /**
     * 管理员强制踢人
     *
     * <p>校验用户存在 → 清除权限缓存 → 移除所有在线会话 → 发布踢人审计事件
     *
     * @param campusId 学校ID
     * @param userId 用户ID
     * @return 被踢出的所有 Token ID 集合
     */
    public Set<String> kick(long campusId, long userId) {
        // 1. 校验用户是否存在（通过适配器查）
        AuthUserCredential credential = authUserDataProvider.getAuthDataByCampusIdAndUserId(campusId, userId);
        if (credential == null) {
            throw new BizException(ResultCode.KICKOUT_FAILED, ResultCode.USER_NOT_FOUND.getMessage());
        }

        // 2. 移除用户权限
        authorizationPort.removeAuthorization(userId, campusId);

        // 3. 移除所有在线会话
        Set<String> sessions = sessionPort.removeAllSessions(campusId, userId);

        if (!CollectionUtils.isEmpty(sessions)) {
            // 4. 记录审计
            for (String tokenId : sessions) {
                eventPublisher.publishEvent(
                    new SessionTerminatedEvent(this, tokenId, LogoutReason.KICK));
            }
            log.info("Kicked {} sessions for user {} campus {}",
                    sessions.size(), userId, campusId);
        } else {
            log.info("kick user {}, campus {}: no online sessions", userId, campusId);
        }

        return sessions;
    }

    // ==================== 刷新 Token ====================

    /**
     * 刷新 Access Token 和 Refresh Token
     * <p>解析 RefreshToken → 校验是否存在 → 生成新 Token 对 → 轮换会话
     *
     * @param command 刷新命令
     * @return 新的Token对
     */
    public RefreshResult refreshAccessToken(RefreshCommand command) {
        // 1.解析refresh token
        RefreshTokenClaims claims = tokenPort.parseRefreshToken(command.getRefreshToken());

        long campusId = claims.getCampusId();
        long userId = claims.getUserId();
        String oldRefreshTokenId = claims.getJti();

        // 2. 获取旧的 accessTokenId（在轮换前记录）
        String oldAccessTokenId = sessionPort
            .getAccessTokenIdByRefreshTokenId(campusId, userId, oldRefreshTokenId);

        // 3. 生成新的token
        String newAccessTokenId = UUID.randomUUID().toString();
        String newRefreshTokenId = UUID.randomUUID().toString();
        long tokenVersion = sessionPort.getTokenVersion(campusId, userId);

        TokenPair tokenPair = tokenPort.generate(
            campusId, userId, claims.getUsername(), claims.getUserType(),
            claims.getDeviceId(), tokenVersion, newAccessTokenId, newRefreshTokenId
        );

        // 4. 原子轮换（内部会校验旧 Token 是否存在）
        sessionPort.rotateRefreshToken(
            campusId, userId,
            oldRefreshTokenId, oldAccessTokenId,
            newRefreshTokenId, newAccessTokenId
        );

        // 5. 发布刷新事件（异步审计更新）
        eventPublisher.publishEvent(new RefreshTokenEvent(
            this, userId, campusId, oldAccessTokenId, newAccessTokenId,
            newRefreshTokenId, tokenPair.getAccessTokenExpiresIn(),
            command.getDeviceType(), command.getUserAgent(), command.getIp()
        ));

        return RefreshResult.builder()
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .expiresIn(tokenPair.getAccessTokenExpiresIn())
                .build();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 执行用户认证
     *
     * <p>获取用户凭证 → 加载领域模型 → 验证密码
     * <p>认证失败时：发布失败事件 → 记录风控 → 根据锁定状态决定抛出类型
     *
     * @param cmd 登录命令对象 {@link LoginCommand}，包含用户名和密码
     * @return {@link AuthUser} 登录成功的用户信息
     * @throws BizException 当认证失败时抛出，用于上层捕获和流程控制
     */
    private AuthUser authenticate(LoginCommand cmd) {
        try {
            // 1. 获取认证数据（通过适配器，无感本地/远程）
            AuthUserCredential credential = authUserDataProvider.getAuthDataByUsername(cmd.getUsername());

            if (credential == null) {
                throw new BizException(ResultCode.AUTH_ACCOUNT_NOT_EXIST);
            }

            // 2. 加载领域模型
            AuthUser user = AuthUser.loadFromCredential(credential);

            // 3. 验证密码（领域逻辑）
            user.verifyPassword(cmd.getPassword(), passwordEncryptor);

            return user;
        } catch (BizException e) {
            // 注意：先记录审计、再处理风控。（即使风控失败也不影响认证异常返回）
            // 1. 登录失败审计
            eventPublisher.publishEvent(
                new LoginFailureEvent(
                    this, cmd.getUsername(), cmd.getIp(), cmd.getDeviceType(),
                    cmd.getUserAgent(), e.getMessage()
                )
            );

            // 登录失败处理（统一收口）
            RiskCheckResult result = riskPort.onFail(cmd.getUsername());
            log.debug("Risk result {}", result);

            if (result.locked()) {
                throw new BizException(ResultCode.AUTH_ACCOUNT_LOCKED);
            }

            throw new BizException(ResultCode.AUTH_USER_LOGIN_ERROR, e);
        }
    }

    /**
     * 登录后置处理
     *
     * <p>风控成功回调 → 多端登录限制 → 保存会话 → 发布成功审计事件
     */
    private void handleLoginSuccess(
        long userId, long campusId, String username,
        String accessTokenId, String refreshTokenId, long expiresIn,
        String deviceType, String userAgent, String ip
    ) {
        // 1. 风控成功处理
        riskPort.onSuccess(username);

        // 2. 限制多端登录
        sessionPort.limitSessions(campusId, userId);

        // 3. 保存会话
        sessionPort.saveSession(campusId, userId, accessTokenId, refreshTokenId);

        // 4. 发布登录成功事件（异步审计）
        LocalDateTime tokenExpiresIn = LocalDateTime.now()
            .plusSeconds(expiresIn);
        eventPublisher.publishEvent(
            new LoginSuccessEvent(this, userId, username,
                accessTokenId, tokenExpiresIn, deviceType, userAgent, ip)
        );
    }

    /**
     * 构建登录响应对象
     *
     * <p>将生成的 {@link TokenPair} 转换为应用层的 {@link LoginResult} 返回给客户端。
     * 负责封装：
     * <ul>
     *     <li>Access Token</li>
     *     <li>Refresh Token</li>
     *     <li>过期时间（秒）</li>
     * </ul>
     *
     * @param tokenPair 登录成功生成的 Token 对象 {@link TokenPair}
     * @return {@link LoginResult} 返回给客户端的登录响应
     */
    private LoginResult buildLoginResult(TokenPair tokenPair) {
        return LoginResult.builder()
            .accessToken(tokenPair.getAccessToken())
            .refreshToken(tokenPair.getRefreshToken())
            .expiresIn(tokenPair.getAccessTokenExpiresIn())
            .build();
    }

}
