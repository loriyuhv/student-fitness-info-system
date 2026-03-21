package com.wsw.fitnesssystem.auth.application.service.impl;

import com.wsw.fitnesssystem.auth.application.authentication.command.LoginCommand;
import com.wsw.fitnesssystem.auth.application.authorization.AuthorizationApplicationService;
import com.wsw.fitnesssystem.auth.application.service.LoginSuccessProcessor;
import com.wsw.fitnesssystem.auth.application.service.RiskControlService;
import com.wsw.fitnesssystem.auth.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.domain.model.TokenPair;
import com.wsw.fitnesssystem.auth.domain.port.SessionRepository;
import com.wsw.fitnesssystem.auth.domain.service.SessionDomainService;
import com.wsw.fitnesssystem.auth.infrastructure.audit.service.LoginAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 登录成功处理器实现类
 *
 * <p>该类负责封装用户登录成功后的所有后置操作，确保登录流程清晰且职责单一。
 * 主要处理包括：
 * <ul>
 *     <li>风控成功处理：重置失败次数并解锁账号</li>
 *     <li>多端登录限制：保证用户同时在线设备数量符合策略</li>
 *     <li>会话持久化：保存 Access Token 与 Refresh Token 关联的会话信息</li>
 *     <li>权限授权：根据用户信息进行一次性授权并更新缓存（懒加载）</li>
 *     <li>登录审计：记录登录成功事件，用于安全审计与统计分析</li>
 * </ul>
 *
 * <p>该实现封装了多个服务，使得应用服务层（LoginApplicationService）只需关注流程编排，
 * 避免直接耦合细节逻辑，提高可读性与可维护性。
 *
 * <p>依赖：
 * <ul>
 *     <li>{@link RiskControlService} - 风控逻辑处理</li>
 *     <li>{@link SessionDomainService} - 多端登录限制策略</li>
 *     <li>{@link SessionRepository} - 会话持久化</li>
 *     <li>{@link AuthorizationApplicationService} - 授权服务</li>
 *     <li>{@link LoginAuditService} - 登录审计</li>
 * </ul>
 * @author loriyuhv
 * @version 1.0 2026/3/21 14:06
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class LoginSuccessProcessorImpl implements LoginSuccessProcessor {

    /** 风控服务，用于处理登录成功后的失败次数重置和解锁 */
    private final RiskControlService riskControlService;

    /** 会话领域服务，用于多端登录限制 */
    private final SessionDomainService sessionDomainService;

    /** 会话持久化接口，用于保存 AccessToken 与 RefreshToken */
    private final SessionRepository sessionRepository;

    /** 授权应用服务，用于执行一次性授权和缓存更新 */
    private final AuthorizationApplicationService authorizationApplicationService;

    /** 登录审计服务，用于记录登录成功事件 */
    private final LoginAuditService loginAuditService;

    /**
     * 处理登录成功后的业务操作
     *
     * <p>处理顺序：
     * <ol>
     *     <li>风控成功处理</li>
     *     <li>限制多端登录</li>
     *     <li>保存会话信息</li>
     *     <li>用户授权（懒加载缓存）</li>
     *     <li>记录登录审计日志</li>
     * </ol>
     *
     * @param user 登录成功的用户信息 {@link AuthUser}
     * @param cmd 登录请求命令对象 {@link LoginCommand}
     * @param tokenPair 生成的 AccessToken 与 RefreshToken 对象 {@link TokenPair}
     */
    @Override
    public void process(AuthUser user, LoginCommand cmd, TokenPair tokenPair) {
        // 1. 风控成功处理
        riskControlService.onSuccess(
            user.getCampusId(),
            user.getUsername()
        );

        // 2. 限制多端登录
        sessionDomainService.limitSessions(
            user.getCampusId(),
            user.getUserId(),
            3
        );

        // 3. 保存会话
        sessionRepository.saveSession(
            user.getCampusId(),
            user.getUserId(),
            tokenPair.getAccessTokenId(),
            tokenPair.getRefreshTokenId()
        );

        // 4. 授权（懒加载缓存）
        authorizationApplicationService.authorize(user);

        // 5. 审计日志
        loginAuditService.loginSuccess(
            user.getUserId(),
            cmd,
            tokenPair
        );
    }
}
