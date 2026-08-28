package com.wsw.fitnesssystem.auth.authentication.application.event;

import com.wsw.fitnesssystem.auth.audit.domain.valueobject.LogoutReason;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 会话终止领域事件（登出/踢人）
 *
 * @author loriyuhv
 * @version 1.0 2026/8/28 16:31
 * @since 1.0
 */
@Getter
public class SessionTerminatedEvent extends ApplicationEvent {

    private final String tokenId;
    private final LogoutReason reason;

    public SessionTerminatedEvent(Object source, String tokenId, LogoutReason reason) {
        super(source);
        this.tokenId = tokenId;
        this.reason = reason;
    }

}
