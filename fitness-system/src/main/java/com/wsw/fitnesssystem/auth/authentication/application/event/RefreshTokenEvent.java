package com.wsw.fitnesssystem.auth.authentication.application.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Refresh Token 轮换事件
 *
 * <p>当 AccessToken 刷新成功时发布，用于更新审计记录中的 tokenId。
 *
 * @author loriyuhv
 * @version 1.0 2026/8/28 22:08
 * @since 1.0
 */
@Getter
public class RefreshTokenEvent extends ApplicationEvent {

    private final Long userId;
    private final Long campusId;
    private final String oldAccessTokenId;
    private final String newAccessTokenId;
    private final String newRefreshTokenId;
    private final Long expiresIn;
    private final String deviceType;
    private final String userAgent;
    private final String ip;

    public RefreshTokenEvent(
        Object source,
        Long userId,
        Long campusId,
        String oldAccessTokenId,
        String newAccessTokenId,
        String newRefreshTokenId,
        Long expiresIn,
        String deviceType,
        String userAgent,
        String ip
    ) {
        super(source);
        this.userId = userId;
        this.campusId = campusId;
        this.oldAccessTokenId = oldAccessTokenId;
        this.newAccessTokenId = newAccessTokenId;
        this.newRefreshTokenId = newRefreshTokenId;
        this.expiresIn = expiresIn;
        this.deviceType = deviceType;
        this.userAgent = userAgent;
        this.ip = ip;
    }

}
