package com.wsw.fitnesssystem.auth.audit.infrastructure.persistence.db.converter;


import com.wsw.fitnesssystem.auth.audit.domain.model.LoginAudit;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.*;
import com.wsw.fitnesssystem.auth.audit.infrastructure.persistence.db.entity.SysUserLogin;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/28 07:48
 * @since 1.0
 */
class LoginAuditConverterTest {

    private static final Long LOGIN_ID = 100L;
    private static final Long USER_ID = 1L;
    private static final String USERNAME = "testUser";
    private static final String TOKEN_ID = "token-123";
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final String DEVICE_TYPE = "WEB";
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String IP = "192.168.1.1";

    // ========== toDomain：已下线记录恢复 ==========

    @Test
    void toDomain_shouldRestoreOfflineRecordWithoutException() {
        // Given
        SysUserLogin entity = createSuccessEntity();
        entity.setLogoutTime(NOW);
        entity.setLogoutReason(LogoutReason.LOGOUT.name());
        entity.setStatus(0); // 离线

        // When
        LoginAudit audit = LoginAuditConverter.toDomain(entity);

        // Then：不抛异常，且状态正确
        assertThat(audit.getStatus()).isEqualTo(OnlineStatus.OFFLINE);
        assertThat(audit.getLogoutTime()).isEqualTo(NOW);
        assertThat(audit.getLogoutReason()).isEqualTo(LogoutReason.LOGOUT);
        assertThat(audit.getId().value()).isEqualTo(LOGIN_ID);
    }

    @Test
    void toDomain_shouldHandleUnknownLogoutReasonGracefully() {
        // Given
        SysUserLogin entity = createSuccessEntity();
        entity.setLogoutTime(NOW);
        entity.setLogoutReason("UNKNOWN_REASON");
        entity.setStatus(0);

        // When
        LoginAudit audit = LoginAuditConverter.toDomain(entity);

        // Then：降级为默认 LOGOUT
        assertThat(audit.getLogoutReason()).isEqualTo(LogoutReason.LOGOUT);
    }

    @Test
    void toDomain_shouldRestoreOnlineRecordCorrectly() {
        // Given
        SysUserLogin entity = createSuccessEntity();
        entity.setStatus(1);
        entity.setLogoutTime(null);
        entity.setLogoutReason(null);

        // When
        LoginAudit audit = LoginAuditConverter.toDomain(entity);

        // Then
        assertThat(audit.isOnline()).isTrue();
        assertThat(audit.getStatus()).isEqualTo(OnlineStatus.ONLINE);
        assertThat(audit.getLogoutTime()).isNull();
        assertThat(audit.getLogoutReason()).isNull();
        assertThat(audit.getTokenId()).isEqualTo(TOKEN_ID);
    }

    @Test
    void toDomain_shouldRestoreFailureRecordCorrectly() {
        // Given
        SysUserLogin entity = createFailureEntity();
        entity.setStatus(0); // 失败记录 status 可能为 0（NEVER_ONLINE）

        // When
        LoginAudit audit = LoginAuditConverter.toDomain(entity);

        // Then
        assertThat(audit.isFailure()).isTrue();
        assertThat(audit.getStatus()).isEqualTo(OnlineStatus.NEVER_ONLINE);
        assertThat(audit.getTokenId()).isNull();
        assertThat(audit.getFailureContext().failReason()).isEqualTo("Password error");
    }

    // ========== toEntity ==========

    @Test
    void toEntity_shouldSetIdWhenPresent() {
        // Given
        LoginAudit audit = createSuccessAudit();
        audit.setId(new AuditId(LOGIN_ID));

        // When
        SysUserLogin entity = LoginAuditConverter.toEntity(audit);

        // Then
        assertThat(entity.getLoginId()).isEqualTo(LOGIN_ID);
    }

    @Test
    void toEntity_shouldSetFieldsCorrectly() {
        // Given
        LoginAudit audit = createSuccessAudit();

        // When
        SysUserLogin entity = LoginAuditConverter.toEntity(audit);

        // Then
        assertThat(entity.getUserId()).isEqualTo(USER_ID);
        assertThat(entity.getUsername()).isEqualTo(USERNAME);
        assertThat(entity.getLoginType()).isEqualTo(1);
        assertThat(entity.getTokenId()).isEqualTo(TOKEN_ID);
        assertThat(entity.getLoginIp()).isEqualTo(IP);
        assertThat(entity.getStatus()).isEqualTo(1); // ONLINE
        assertThat(entity.getLogoutTime()).isNull();
        assertThat(entity.getLogoutReason()).isNull();
    }

    // ========== 辅助方法 ==========

    private SysUserLogin createSuccessEntity() {
        SysUserLogin entity = new SysUserLogin();
        entity.setLoginId(LOGIN_ID);
        entity.setUserId(USER_ID);
        entity.setUsername(USERNAME);
        entity.setLoginType(1);
        entity.setTokenId(TOKEN_ID);
        entity.setDeviceType(DEVICE_TYPE);
        entity.setClientInfo(USER_AGENT);
        entity.setLoginIp(IP);
        entity.setLoginTime(NOW);
        entity.setExpireTime(NOW.plusHours(2));
        entity.setStatus(1);
        return entity;
    }

    private SysUserLogin createFailureEntity() {
        SysUserLogin entity = new SysUserLogin();
        entity.setLoginId(LOGIN_ID + 1);
        entity.setUsername(USERNAME);
        entity.setLoginType(0);
        entity.setFailReason("Password error");
        entity.setDeviceType(DEVICE_TYPE);
        entity.setClientInfo(USER_AGENT);
        entity.setLoginIp(IP);
        entity.setLoginTime(NOW);
        entity.setStatus(0);
        return entity;
    }

    private LoginAudit createSuccessAudit() {
        return LoginAudit.recordSuccess(
            USER_ID,
            USERNAME,
            TOKEN_ID,
            NOW.plusHours(2),
            new DeviceInfo(DEVICE_TYPE, USER_AGENT),
            new IpAddress(IP)
        );
    }

}