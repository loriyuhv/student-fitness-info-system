package com.wsw.fitnesssystem.auth.authentication.application.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 登录失败领域事件
 *
 * @author loriyuhv
 * @version 1.0 2026/8/28 16:30
 * @since 1.0
 */
@Getter
public class LoginFailureEvent extends ApplicationEvent {

    private final String username;
    private final String ip;
    private final String deviceType;
    private final String userAgent;
    private final String failReason;

    public LoginFailureEvent(
        Object source, String username, String ip, String deviceType, String userAgent, String failReason
    ) {
        super(source);
        this.username = username;
        this.ip = ip;
        this.deviceType = deviceType;
        this.userAgent = userAgent;
        this.failReason = failReason;
    }

}
