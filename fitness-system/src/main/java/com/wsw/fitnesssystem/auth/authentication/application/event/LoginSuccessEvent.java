package com.wsw.fitnesssystem.auth.authentication.application.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 登录成功领域事件
 *
 * <p>由 AuthApplicationService 发布，由审计、风控、会话等模块监听处理。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/28 16:27
 * @since 1.0
 */
@Getter
public class LoginSuccessEvent extends ApplicationEvent {

    private final Long userId;
    private final String username;
    private final String tokenId;
    private final LocalDateTime expireTime;
    private final String deviceType;
    private final String userAgent;
    private final String ip;

    public LoginSuccessEvent(
        Object source, Long userId, String username, String tokenId,
        LocalDateTime expireTime, String deviceType, String userAgent, String ip
    ) {
        super(source);
        this.userId = userId;
        this.username = username;
        this.tokenId = tokenId;
        this.expireTime = expireTime;
        this.deviceType = deviceType;
        this.userAgent = userAgent;
        this.ip = ip;
    }

}
