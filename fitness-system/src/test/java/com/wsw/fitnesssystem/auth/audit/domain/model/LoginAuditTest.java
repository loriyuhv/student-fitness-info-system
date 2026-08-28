package com.wsw.fitnesssystem.auth.audit.domain.model;

import com.wsw.fitnesssystem.auth.audit.domain.valueobject.*;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link LoginAudit} 单元测试
 *
 * @author loriyuhv
 * @version 1.0 2026/8/28 07:25
 * @since 1.0
 */
class LoginAuditTest {

    private static final Long USER_ID = 100L;
    private static final String USERNAME = "testUser";
    private static final String TOKEN_ID = "token-123";
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final LocalDateTime EXPIRE_TIME = NOW.plusHours(2);
    private static final DeviceInfo DEVICE = new DeviceInfo("WEB", "Mozilla/5.0");
    private static final IpAddress IP = new IpAddress("192.168.1.1");

    // ========== 测试 recordSuccess ==========

    @Test
    void recordSuccess_shouldCreateOnlineAudit() {
        // Given
        // When
        LoginAudit audit = LoginAudit.recordSuccess(USER_ID, USERNAME, TOKEN_ID, EXPIRE_TIME, DEVICE, IP);

        // Then
        assertThat(audit.getUserId()).isEqualTo(USER_ID);
        assertThat(audit.getUsername()).isEqualTo(USERNAME);
        assertThat(audit.getTokenId()).isEqualTo(TOKEN_ID);
        assertThat(audit.getTokenSnapshot().expireTime()).isEqualTo(EXPIRE_TIME);
        assertThat(audit.getDevice()).isEqualTo(DEVICE);
        assertThat(audit.getIp()).isEqualTo(IP);
        assertThat(audit.getLoginTime()).isNotNull();
        assertThat(audit.getResult()).isEqualTo(LoginResult.SUCCESS);
        assertThat(audit.getStatus()).isEqualTo(OnlineStatus.ONLINE);
        assertThat(audit.isOnline()).isTrue();
        assertThat(audit.isFailure()).isFalse();
        assertThat(audit.hasToken()).isTrue();
        assertThat(audit.getLogoutTime()).isNull();
        assertThat(audit.getLogoutReason()).isNull();
    }

    @Test
    void recordSuccess_shouldThrowWhenUserIdNull() {
        assertThatThrownBy(
            () -> LoginAudit.recordSuccess(null, USERNAME, TOKEN_ID, EXPIRE_TIME, DEVICE, IP))
            .isInstanceOf(BizException.class)
            .extracting("resultCode")
            .isEqualTo(ResultCode.AUTH_USER_NOT_FOUND);
    }

    @Test
    void recordSuccess_shouldThrowWhenTokenIdBlank() {
        assertThatThrownBy(
            () -> LoginAudit.recordSuccess(USER_ID, USERNAME, "", EXPIRE_TIME, DEVICE, IP))
            .isInstanceOf(BizException.class)
            .extracting("resultCode")
            .isEqualTo(ResultCode.TOKEN_INVALID);
    }

    // ========== 测试 recordFailure ==========

    @Test
    void recordFailure_shouldCreateNeverOnlineAudit() {
        // Given
        String failReason = "Password error";

        // When
        LoginAudit audit = LoginAudit.recordFailure(USERNAME, IP, DEVICE, failReason);

        // Then
        assertThat(audit.getUsername()).isEqualTo(USERNAME);
        assertThat(audit.getUserId()).isNull();
        assertThat(audit.getTokenId()).isNull();
        assertThat(audit.getFailureContext().failReason()).isEqualTo(failReason);
        assertThat(audit.getResult()).isEqualTo(LoginResult.FAILURE);
        assertThat(audit.getStatus()).isEqualTo(OnlineStatus.NEVER_ONLINE);
        assertThat(audit.isOnline()).isFalse();
        assertThat(audit.isFailure()).isTrue();
        assertThat(audit.hasToken()).isFalse();
        assertThat(audit.getLogoutTime()).isNull();
        assertThat(audit.getLogoutReason()).isNull();
    }


// ========== 测试 terminate ==========

    @Test
    void terminate_shouldMarkOfflineWhenOnline() {
        // Given
        LoginAudit audit = createOnlineAudit();

        // When
        audit.terminate(LogoutReason.LOGOUT);

        // Then
        assertThat(audit.getStatus()).isEqualTo(OnlineStatus.OFFLINE);
        assertThat(audit.isOnline()).isFalse();
        assertThat(audit.getLogoutTime()).isNotNull();
        assertThat(audit.getLogoutReason()).isEqualTo(LogoutReason.LOGOUT);
    }

    @Test
    void terminate_shouldThrowWhenAlreadyOffline() {
        // Given
        LoginAudit audit = createOnlineAudit();
        audit.terminate(LogoutReason.LOGOUT); // 第一次下线

        // When & Then
        assertThatThrownBy(() -> audit.terminate(LogoutReason.KICK))
            .isInstanceOf(BizException.class)
            .extracting("resultCode")
            .isEqualTo(ResultCode.SESSION_ALREADY_OFFLINE);
    }

    @Test
    void terminate_shouldThrowWhenNeverOnline() {
        // Given
        LoginAudit audit = LoginAudit.recordFailure(USERNAME, IP, DEVICE, "Error");

        // When & Then
        assertThatThrownBy(() -> audit.terminate(LogoutReason.LOGOUT))
            .isInstanceOf(BizException.class)
            .extracting("resultCode")
            .isEqualTo(ResultCode.AUTH_USER_NOT_LOGIN);
    }

    // ========== 测试 markOffline ==========

    @Test
    void markOffline_shouldForceOfflineWithoutValidation() {
        // Given
        LoginAudit audit = LoginAudit.recordFailure(USERNAME, IP, DEVICE, "Error");
        LocalDateTime logoutTime = LocalDateTime.now();

        // When
        audit.markOffline(LogoutReason.KICK, logoutTime);

        // Then
        assertThat(audit.getStatus()).isEqualTo(OnlineStatus.OFFLINE);
        assertThat(audit.getLogoutTime()).isEqualTo(logoutTime);
        assertThat(audit.getLogoutReason()).isEqualTo(LogoutReason.KICK);
        // 即使 NEVER_ONLINE 也能强制下线，不抛异常
    }

    // ========== 辅助方法 ==========

    private LoginAudit createOnlineAudit() {
        return LoginAudit.recordSuccess(USER_ID, USERNAME, TOKEN_ID, EXPIRE_TIME, DEVICE, IP);
    }

}