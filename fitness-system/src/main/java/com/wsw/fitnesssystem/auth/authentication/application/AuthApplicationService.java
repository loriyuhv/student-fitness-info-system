package com.wsw.fitnesssystem.auth.authentication.application;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wsw.fitnesssystem.auth.audit.application.AuditAppService;
import com.wsw.fitnesssystem.auth.authentication.application.dto.command.LoginCommand;
import com.wsw.fitnesssystem.auth.authentication.application.dto.command.RefreshCommand;
import com.wsw.fitnesssystem.auth.authentication.application.dto.port.AuthUserCredential;
import com.wsw.fitnesssystem.auth.authentication.application.dto.result.LoginResult;
import com.wsw.fitnesssystem.auth.authentication.application.dto.result.RefreshResult;
import com.wsw.fitnesssystem.auth.authentication.application.port.AuthUserDataProvider;
import com.wsw.fitnesssystem.auth.authentication.application.port.AuthorizationPort;
import com.wsw.fitnesssystem.auth.authentication.application.port.SessionPort;
import com.wsw.fitnesssystem.auth.authentication.domain.port.PasswordEncryptor;
import com.wsw.fitnesssystem.auth.authentication.application.service.LoginSuccessProcessor;
import com.wsw.fitnesssystem.auth.risk.application.RiskControlService;
import com.wsw.fitnesssystem.auth.authentication.application.port.TokenPort;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.LogoutReason;
import com.wsw.fitnesssystem.auth.authentication.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.authentication.application.dto.port.TokenPair;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.RiskFailResult;
import com.wsw.fitnesssystem.auth.authentication.application.dto.port.RefreshTokenClaims;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final TokenPort tokenPort;
    private final SessionPort sessionPort;
    private final AuditAppService auditAppService;
    private final AuthorizationPort authorizationPort;
    private final PasswordEncryptor passwordEncryptor;
    private final RiskControlService riskControlService;
    private final AuthUserDataProvider authUserDataProvider;
    private final LoginSuccessProcessor loginSuccessProcessor;

    /**
     * 用户登录流程（应用服务入口）
     *
     * <p>该方法负责用户登录的完整应用层流程，遵循分阶段编排：
     * <ol>
     *     <li>风控前置检查：校验账号是否被锁定及失败次数限制 {@link RiskControlService#preCheck(String)}</li>
     *     <li>用户认证：调用领域服务验证用户名和密码 {@link #authenticate(LoginCommand)}</li>
     *     <li>生成 Token：生成 Access Token 与 Refresh Token {@link TokenPort}</li>
     *     <li>登录成功后处理：多端限制、会话持久化、审计 {@link LoginSuccessProcessor}</li>
     *     <li>构建返回结果：封装登录响应 {@link #buildLoginResult(TokenPair)}</li>
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
     * @return {@link LoginResult} 登录响应对象，包含 Access Token、Refresh Token、Token ID 及过期时间
     * @throws BizException 当用户名或密码错误，或其他业务异常时抛出
     */
    public LoginResult login(LoginCommand cmd) {
        /* 1. 风控前置检查 */
        riskControlService.preCheck(cmd.getUsername());

        /* 2. 认证 */
        AuthUser user = authenticate(cmd);

        // 3. 生成 Token
        String accessTokenId = UUID.randomUUID().toString();
        String refreshTokenId = UUID.randomUUID().toString();
        Operator operator = new Operator(
            user.getCampusId(), user.getUserId(), user.getUsername(), user.getUserType()
        );
        long campusId = user.getCampusId();
        long userId = user.getUserId();
        String username = user.getUsername();
        int userType = user.getUserType();

        long tokenVersion = sessionPort.getTokenVersion(campusId, userId);
        TokenPair tokenPair = tokenPort.generate(
            campusId, userId, username, userType, cmd.getDeviceId(),
            tokenVersion, accessTokenId, refreshTokenId
        );

        // 4. 登录成功后处理
        loginSuccessProcessor.process(operator, cmd, tokenPair);

        // 5. 返回
        return buildLoginResult(tokenPair);
    }

    /***
     * 用户登出
     */
    public void logout(Operator operator, String accessTokenId) {
        // 1. 将当前 accessToken 加入黑名单
        sessionPort.addToBlacklist(accessTokenId);

        // 2. 从 ZSET 中删除（会话下线）
        sessionPort.removeSession(operator.campusId(), operator.userId(), accessTokenId);

        // 3. 记录登出审计
        auditAppService.terminateSession(accessTokenId, LogoutReason.LOGOUT);
    }

    /**
     * 踢人操作（管理员使用）
     *
     * @param campusId 学校ID
     * @param userId 用户ID
     * @return 用户TokenID集合
     */
    public Set<String> kick(long campusId, long userId) {
        // 1. 校验用户是否存在（通过适配器查）
        AuthUserCredential credential = authUserDataProvider.getAuthDataByCampusIdAndUserId(campusId, userId);
        if (credential == null) {
            throw new BizException(ResultCode.KICKOUT_FAILED, ResultCode.USER_NOT_EXIST.getMessage());
        }

        // 2. 移除用户权限
        authorizationPort.removeAuthorization(userId, campusId);

        //2. 移除所有在线会话
        Set<String> onlineSessions = sessionPort.removeAllSessions(campusId, userId);

        if (!CollectionUtils.isEmpty(onlineSessions)) {
            for (String tokenId : onlineSessions) {
                // 3. 记录审计
                auditAppService.terminateSession(tokenId, LogoutReason.KICK);
            }
            log.info("Kicked {} sessions for user {} campus {}",
                    onlineSessions.size(), userId, campusId);
        } else {
            log.info("kick user {}, campus {}: no online sessions", userId, campusId);
        }

        return onlineSessions;
    }

    public RefreshResult refreshAccessToken(RefreshCommand command) {
        // 1.解析refresh token
        RefreshTokenClaims claims = tokenPort.parseRefreshToken(command.getRefreshToken());

        long campusId = claims.getCampusId();
        long userId = claims.getUserId();
        String username = claims.getUsername();
        int userType = claims.getUserType();
        String oldRefreshTokenId = claims.getJti();
        String oldAccessTokenId = sessionPort
            .getAccessTokenIdByRefreshTokenId(campusId, userId, oldRefreshTokenId);

        // 2. 校验refresh session
        boolean result = sessionPort.existsRefreshToken(campusId, userId, oldRefreshTokenId);
        if (!result) {
            throw new BizException(ResultCode.REFRESH_TOKEN_INVALID);
        }

        // 3.生成新的token
        String newAccessTokenId = UUID.randomUUID().toString();
        String newRefreshTokenId = UUID.randomUUID().toString();
        long tokenVersion = sessionPort.getTokenVersion(campusId, userId);
        TokenPair tokenPair = tokenPort.generate(
            campusId, userId, username, userType, claims.getDeviceId(),
            tokenVersion, newAccessTokenId, newRefreshTokenId
        );

        // 4.Refresh Token Rotation
        sessionPort.rotateRefreshToken(
            campusId, userId, oldRefreshTokenId, oldAccessTokenId,
            newRefreshTokenId, newAccessTokenId
        );

        return RefreshResult.builder()
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .expiresIn(tokenPair.getAccessTokenExpiresIn())
                .build();
    }

    /**
     * 执行用户认证
     *
     * <p>完成用户认证，认证成功返回 {@link AuthUser}；认证失败则通过
     * {@link RiskControlService#onFail(String)} 统一处理失败计数和锁定策略，然后抛出异常。
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
            // 1. 获取认证数据（通过适配器，无感本地/远程）
            AuthUserCredential credential = authUserDataProvider.getAuthDataByUsername(cmd.getUsername());

            if (credential == null) {
                throw new BizException(ResultCode.USER_NOT_EXIST);
            }

            // 2. 加载领域模型
            AuthUser user = AuthUser.loadFromCredential(credential);

            // 3. 验证密码（领域逻辑）
            user.verifyPassword(cmd.getPassword(), passwordEncryptor);

            return user;
        } catch (BizException ex) {
            // 登录失败处理（统一收口）
            RiskFailResult result = riskControlService.onFail(cmd.getUsername());

            log.debug("result {}", result);

            // 登录失败审计
            auditAppService.recordLoginFailure(
                cmd.getUsername(), cmd.getIp(), cmd.getDeviceType(),
                cmd.getUserAgent(), ex.getMessage()
            );
            throw new BizException(ResultCode.USER_LOGIN_ERROR);
        }
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
