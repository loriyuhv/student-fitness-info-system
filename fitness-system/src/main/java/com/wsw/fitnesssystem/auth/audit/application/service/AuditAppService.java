package com.wsw.fitnesssystem.auth.audit.application.service;

import com.wsw.fitnesssystem.auth.audit.domain.valueobject.LogoutReason;

import java.time.LocalDateTime;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/28 16:43
 * @since 1.0
 */
public interface AuditAppService {

    void recordLoginSuccess(
        Long userId, String username, String tokenId,
        LocalDateTime expireTime, String deviceType, String userAgent, String ip
    );

    void recordLoginFailure(
        String username, String ip, String deviceType, String userAgent, String failReason
    );

    void terminateSession(String tokenId, LogoutReason reason);

}
