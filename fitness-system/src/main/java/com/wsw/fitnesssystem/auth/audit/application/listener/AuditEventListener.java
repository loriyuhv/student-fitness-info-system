package com.wsw.fitnesssystem.auth.audit.application.listener;

import com.wsw.fitnesssystem.auth.audit.application.service.AuditAppService;
import com.wsw.fitnesssystem.auth.authentication.application.event.LoginFailureEvent;
import com.wsw.fitnesssystem.auth.authentication.application.event.LoginSuccessEvent;
import com.wsw.fitnesssystem.auth.authentication.application.event.RefreshTokenEvent;
import com.wsw.fitnesssystem.auth.authentication.application.event.SessionTerminatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 审计事件监听器
 *
 * <p>监听认证模块发布的事件，异步记录审计日志。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/28 16:42
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditAppService auditAppService;

    @Async
    @EventListener
    public void handleLoginSuccess(LoginSuccessEvent event) {
        log.debug("收到登录成功事件: userId={}", event.getUserId());
        auditAppService.recordLoginSuccess(
            event.getUserId(),
            event.getUsername(),
            event.getTokenId(),
            event.getExpireTime(),
            event.getDeviceType(),
            event.getUserAgent(),
            event.getIp());
    }

    @Async
    @EventListener
    public void handleLoginFailure(LoginFailureEvent event) {
        log.debug("收到登录失败事件: username={}", event.getUsername());
        auditAppService.recordLoginFailure(
            event.getUsername(),
            event.getIp(),
            event.getDeviceType(),
            event.getUserAgent(),
            event.getFailReason()
        );
    }

    @Async
    @EventListener
    public void handleSessionTerminated(SessionTerminatedEvent event) {
        log.debug("收到会话终止事件: tokenId={}, reason={}", event.getTokenId(), event.getReason());
        auditAppService.terminateSession(event.getTokenId(), event.getReason());
    }

    @Async
    @EventListener
    public void handleRefreshToken(RefreshTokenEvent event) {
        log.debug("收到刷新Token事件: userId={}, oldTokenId={}, newTokenId={}",
            event.getUserId(), event.getOldAccessTokenId(), event.getNewAccessTokenId());

        LocalDateTime newExpireTime = LocalDateTime.now().plusSeconds(event.getExpiresIn());

        // 调用审计服务更新 tokenId
        auditAppService.updateTokenId(
            event.getOldAccessTokenId(),
            event.getNewAccessTokenId(),
            newExpireTime
        );
    }

}
