package com.wsw.fitnesssystem.iam.authentication.application.orchestrator;

import com.wsw.fitnesssystem.iam.authentication.application.dto.command.LoginCommand;
import com.wsw.fitnesssystem.iam.authentication.application.dto.result.LoginResult;
import com.wsw.fitnesssystem.iam.authentication.application.event.UserLoggedInEvent;
import com.wsw.fitnesssystem.iam.authentication.application.event.UserLoginFailedEvent;
import com.wsw.fitnesssystem.iam.authentication.application.port.output.RiskPort;
import com.wsw.fitnesssystem.iam.authentication.application.port.output.SessionPort;
import com.wsw.fitnesssystem.iam.authentication.application.port.output.TokenPort;
import com.wsw.fitnesssystem.iam.authentication.application.port.output.dto.RiskCheckResult;
import com.wsw.fitnesssystem.iam.authentication.application.port.output.dto.TokenPair;
import com.wsw.fitnesssystem.iam.authentication.domain.model.AuthAccount;
import com.wsw.fitnesssystem.iam.authentication.domain.port.PasswordEncryptorPort;
import com.wsw.fitnesssystem.iam.authentication.domain.repository.AuthAccountRepository;
import com.wsw.fitnesssystem.iam.error.IamAuthNErrorCode;
import com.wsw.fitnesssystem.iam.error.IamRiskErrorCode;
import com.wsw.fitnesssystem.shared.kernel.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 登录流程编排器
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>串联登录完整流程：风控前置 → 认证 → 生成 Token → 后置处理 → 返回结果</li>
 *   <li>编排过程中涉及的端口调用、领域行为、事件发布统一收口在本类</li>
 *   <li>认证失败的统一收口逻辑（失败事件 + 风控处理 + 异常抛出）也在此类中</li>
 * </ul>
 *
 * <p><b>与 LoginCommandService 的职责区分：</b>
 * <ul>
 *   <li>{@code LoginCommandService} 只做用例入口，表达“有一个登录用例被调用”</li>
 *   <li>{@code LoginOrchestrator} 承载多步骤编排，表达“登录用例具体怎么执行”</li>
 * </ul>
 *
 * <p><b>编排步骤：</b>
 * <ol>
 *   <li>风控前置检查（阻断黑名单 / 已锁定账号）</li>
 *   <li>加载账号 + 密码校验（领域行为）</li>
 *   <li>生成 Token 对（含 accessTokenId / refreshTokenId / tokenVersion）</li>
 *   <li>登录成功：风控成功回调 → 多端登录限制 → 保存会话 → 发布成功事件</li>
 *   <li>组装并返回 LoginResult</li>
 * </ol>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21 10:53
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginOrchestrator {

    private final RiskPort riskPort;
    private final TokenPort tokenPort;
    private final SessionPort sessionPort;
    private final PasswordEncryptorPort passwordEncryptorPort;
    private final AuthAccountRepository authAccountRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 执行登录编排
     *
     * @param command 登录业务指令
     * @return 登录结果
     */
    public LoginResult execute(LoginCommand command) {
        // 1. 风控前置检查
        riskPort.preCheck(command.username());

        // 2. 用户认证（加载账号 + 密码校验 + 失败统一收口）
        AuthAccount account = authenticate(command);

        // 3. 生成 Token 对
        String accessTokenId = UUID.randomUUID().toString();
        String refreshTokenId = UUID.randomUUID().toString();
        long userId = account.getUserId();
        long campusId = account.getCampusId();
        long tokenVersion = sessionPort.getTokenVersion(campusId, userId);

        TokenPair tokenPair = tokenPort.generate(
            campusId, userId, account.getUsername(), account.getUserType().getCode(),
            command.deviceId(), tokenVersion, accessTokenId, refreshTokenId
        );

        // 4. 登录成功 → 后置处理（风控 + 会话 + 事件）
        handleLoginSuccess(
            userId, campusId, account.getUsername(), accessTokenId, refreshTokenId,
            tokenPair.getAccessTokenExpiresIn(),
            command.deviceType(), command.userAgent(), command.ip()
        );

        // 5. 组装返回
        return toLoginResult(tokenPair);
    }

    /**
     * 执行用户认证
     *
     * <p>获取用户凭证 → 加载领域模型 → 验证密码。
     * <p>认证失败时：发布失败事件 → 记录风控 → 根据锁定状态决定抛出类型。
     *
     * @param command 登录命令
     * @return 认证成功的账号聚合根
     * @throws BizException 认证失败
     */
    private AuthAccount authenticate(LoginCommand command) {
        try {
            // 1. 加载账号
            AuthAccount account = authAccountRepository.findByUsername(command.username())
                .orElseThrow(() -> new BizException(IamAuthNErrorCode.ACCOUNT_NOT_EXIST));

            // 2. 验证密码（领域行为）
            account.verifyPassword(command.password(), passwordEncryptorPort);

            return account;
        } catch (BizException e) {
            // 1. 登录失败审计事件（先记录审计，保证即使风控失败也不影响认证异常返回）
            eventPublisher.publishEvent(
                new UserLoginFailedEvent(
                    this, command.username(), command.ip(),
                    command.deviceType(), command.userAgent(),
                    e.getErrorCode().code()
                )
            );

            // 2. 风控失败处理（统一收口）
            RiskCheckResult result = riskPort.onFail(command.username());
            log.debug("Risk check result: {}", result);

            if (result.locked()) {
                throw new BizException(IamRiskErrorCode.ACCOUNT_LOCKED);
            }

            throw new BizException(IamAuthNErrorCode.USER_LOGIN_ERROR, e);
        }
    }

    /**
     * 登录成功后的后置处理
     *
     * <p>风控成功回调 → 多端登录限制 → 保存会话 → 发布登录成功事件。
     */
    private void handleLoginSuccess(
        long userId, long campusId, String username,
        String accessTokenId, String refreshTokenId, long expiresIn,
        String deviceType, String userAgent, String ip
    ) {
        // 1. 风控成功处理（清零失败计数）
        riskPort.onSuccess(username);

        // 2. 多端登录限制
        sessionPort.limitSessions(campusId, userId);

        // 3. 保存会话
        sessionPort.saveSession(campusId, userId, accessTokenId, refreshTokenId);

        // 4. 发布登录成功事件（异步审计）
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(expiresIn);
        eventPublisher.publishEvent(
            new UserLoggedInEvent(this, userId, username, accessTokenId,
                expireTime, deviceType, userAgent, ip)
        );

        log.info("Login succeeded: userId={}, campusId={}, tokenId={}",
            userId, campusId, accessTokenId);
    }

    /**
     * 将 TokenPair 转换为应用层 LoginResult
     */
    private LoginResult toLoginResult(TokenPair tokenPair) {
        return new LoginResult(
            tokenPair.getAccessToken(),
            tokenPair.getRefreshTokenId(),
            tokenPair.getRefreshTokenExpiresIn()
        );
    }

}
