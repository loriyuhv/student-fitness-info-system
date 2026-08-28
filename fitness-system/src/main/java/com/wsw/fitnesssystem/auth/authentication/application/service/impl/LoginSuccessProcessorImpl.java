package com.wsw.fitnesssystem.auth.authentication.application.service.impl;

import com.wsw.fitnesssystem.auth.authentication.application.dto.command.LoginCommand;
import com.wsw.fitnesssystem.auth.authentication.application.event.LoginSuccessEvent;
import com.wsw.fitnesssystem.auth.authentication.application.port.RiskPort;
import com.wsw.fitnesssystem.auth.authentication.application.port.SessionPort;
import com.wsw.fitnesssystem.auth.authentication.application.service.LoginSuccessProcessor;
import com.wsw.fitnesssystem.auth.authentication.application.dto.port.TokenPair;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录成功处理器实现类
 *
 * <p>该类负责封装用户登录成功后的所有后置操作，确保登录流程清晰且职责单一。
 * 主要处理包括：
 * <ul>
 *     <li>风控成功处理：重置失败次数并解锁账号</li>
 *     <li>多端登录限制：保证用户同时在线设备数量符合策略</li>
 *     <li>会话持久化：保存 Access Token 与 Refresh Token 关联的会话信息</li>
 *     <li>登录审计：记录登录成功事件，用于安全审计与统计分析</li>
 * </ul>
 *
 * <p>该实现封装了多个服务，使得应用服务层（LoginApplicationService）只需关注流程编排，
 * 避免直接耦合细节逻辑，提高可读性与可维护性。
 *
 * <p>依赖：
 * <ul>
 *     <li>{@link RiskPort} - 风控逻辑处理</li>
 *     <li>{@link SessionPort} - 多端登录限制策略；会话持久化</li>
 *     <li>{@link ApplicationEventPublisher} - 登录审计</li>
 * </ul>
 * @author loriyuhv
 * @version 1.0 2026/3/21 14:06
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class LoginSuccessProcessorImpl implements LoginSuccessProcessor {

    /** 风控端口，用于处理登录成功后的失败次数重置和解锁 */
    private final RiskPort riskPort;

    /** 会话领域端口，用于多端登录限制； 用于保存 AccessToken 与 RefreshToken */
    private final SessionPort sessionPort;

    /** 登录审计服务，用于记录登录成功事件 */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 处理登录成功后的业务操作
     *
     * <p>处理顺序：
     * <ol>
     *     <li>风控成功处理</li>
     *     <li>限制多端登录</li>
     *     <li>保存会话信息</li>
     *     <li>记录登录审计日志</li>
     * </ol>
     *
     * @param operator 操作者 {@link Operator}
     * @param cmd 登录请求命令对象 {@link LoginCommand}
     * @param tokenPair 生成的 AccessToken 与 RefreshToken 对象 {@link TokenPair}
     */
    @Override
    public void process(Operator operator, LoginCommand cmd, TokenPair tokenPair) {
        // 1. 风控成功处理
        riskPort.onSuccess(operator.username());

        // 2. 限制多端登录
        sessionPort.limitSessions(operator.campusId(), operator.userId());

        // 3. 保存会话
        sessionPort.saveSession(
            operator.campusId(), operator.userId(),
            tokenPair.getAccessTokenId(), tokenPair.getRefreshTokenId()
        );

        // 4. 审计日志 （应用层编排，异步执行）
        LocalDateTime tokenExpiresIn = LocalDateTime.now()
            .plusSeconds(tokenPair.getAccessTokenExpiresIn());
        eventPublisher.publishEvent(
            new LoginSuccessEvent(
                this, operator.userId(), operator.username(), tokenPair.getAccessTokenId(),
                tokenExpiresIn, cmd.getDeviceType(), cmd.getUserAgent(), cmd.getIp()
            ));
    }

}
