package com.wsw.fitnesssystem.iam.authentication.application.service.command;

import com.wsw.fitnesssystem.iam.audit.domain.valueobject.LogoutReason;
import com.wsw.fitnesssystem.iam.authentication.application.dto.command.LogoutCommand;
import com.wsw.fitnesssystem.iam.session.application.event.SessionTerminatedEvent;
import com.wsw.fitnesssystem.iam.authentication.application.port.output.SessionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 登出用例入口（Application 层写服务）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>处理当前登录用户的主动登出流程</li>
 *   <li>步骤简单（黑名单 → 移除会话 → 事件），不额外抽 Orchestrator</li>
 * </ul>
 *
 * <p><b>流程：</b>
 * <ol>
 *   <li>将当前 AccessToken 加入黑名单，使其立即失效</li>
 *   <li>从在线会话集合中移除该 AccessToken</li>
 *   <li>发布 {@link SessionTerminatedEvent}（reason=LOGOUT）用于审计</li>
 * </ol>
 *
 * <p><b>事件引用说明：</b>
 * 本方法发布的 {@link SessionTerminatedEvent} 属于 session 子域的事件类型，
 * 因为会话终止是会话生命周期事件，与踢人事件共用同一契约。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21 10:51
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutCommandService {

    private final SessionPort sessionPort;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 用户主动登出
     *
     * @param command 登出命令
     */
    public void logout(LogoutCommand command) {
        // 1. 加入黑名单
        sessionPort.addToBlacklist(command.accessTokenId());

        // 2. 从在线会话中移除
        sessionPort.removeSession(command.campusId(), command.userId(), command.accessTokenId());

        // 3. 发布登出事件（异步审计）
        eventPublisher.publishEvent(
            new SessionTerminatedEvent(
                this,
                command.campusId(),
                command.userId(),
                command.accessTokenId(),
                LogoutReason.LOGOUT
            )
        );

        log.info("Logout succeeded: userId={}, campusId={}, tokenId={}",
            command.campusId(), command.userId(), command.accessTokenId()
        );
    }

}
