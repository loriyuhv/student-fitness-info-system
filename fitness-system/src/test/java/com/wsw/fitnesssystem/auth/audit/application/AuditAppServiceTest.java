package com.wsw.fitnesssystem.auth.audit.application;

import com.wsw.fitnesssystem.auth.audit.domain.model.LoginAudit;
import com.wsw.fitnesssystem.auth.audit.domain.port.LoginAuditRepository;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.DeviceInfo;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.IpAddress;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.LogoutReason;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.OnlineStatus;
import org.apache.ibatis.exceptions.PersistenceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/28 11:16
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class AuditAppServiceTest {

    @Mock
    private LoginAuditRepository auditRepository;

    @InjectMocks
    private AuditAppService auditAppService;

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "testUser";
    private static final String TOKEN_ID = "token-123";
    private static final String IP = "192.168.1.1";
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String DEVICE_TYPE = "WEB";
    private static final String FAIL_REASON = "Password error";

    // ========== recordLoginSuccess ==========

    @Test
    void recordLoginSuccess_shouldSaveAudit() {
        // Given
        LocalDateTime expireTime = LocalDateTime.now().plusHours(2);

        // When
        auditAppService.recordLoginSuccess(
            USER_ID, USERNAME, TOKEN_ID, expireTime, DEVICE_TYPE, USER_AGENT, IP);

        // Then
        ArgumentCaptor<LoginAudit> captor = ArgumentCaptor.forClass(LoginAudit.class);
        verify(auditRepository, times(1)).save(captor.capture());
        LoginAudit saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getTokenId()).isEqualTo(TOKEN_ID);
        assertThat(saved.getDevice().deviceType()).isEqualTo(DEVICE_TYPE);
        assertThat(saved.getIp().value()).isEqualTo(IP);
    }

    @Test
    void recordLoginSuccess_shouldLogWarnWhenBizException() {
        // Given：传入 null userId 触发 BizException
        LocalDateTime expireTime = LocalDateTime.now().plusHours(2);

        // When
        auditAppService.recordLoginSuccess(
            null, USERNAME, TOKEN_ID, expireTime, DEVICE_TYPE, USER_AGENT, IP);

        // Then：Repository 不应该被调用
        verify(auditRepository, never()).save(any());
        // 日志会打印 WARN（实际无法断言日志，但代码执行无异常）
    }

    @Test
    void recordLoginSuccess_shouldLogErrorWhenSystemException() {
        // Given：模拟仓储入库抛出数据库运行时异常
        // 注意：不能抛出SQLException（受检异常），MyBatis会将底层SQL异常包装成运行时异常PersistenceException
        doThrow(new PersistenceException("DB error")).when(auditRepository).save(any(LoginAudit.class));

        // When
        auditAppService.recordLoginSuccess(
            USER_ID, USERNAME, TOKEN_ID, LocalDateTime.now(), DEVICE_TYPE, USER_AGENT, IP);

        // Then：方法不向上抛异常（异步吞掉），但日志会打印 ERROR
        verify(auditRepository, times(1)).save(any());
    }

    // ========== recordLoginFailure ==========

    @Test
    void recordLoginFailure_shouldSaveAudit() {
        // When
        auditAppService.recordLoginFailure(USERNAME, IP, DEVICE_TYPE, USER_AGENT, FAIL_REASON);

        // Then
        ArgumentCaptor<LoginAudit> captor = ArgumentCaptor.forClass(LoginAudit.class);
        verify(auditRepository, times(1)).save(captor.capture());
        LoginAudit saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo(USERNAME);
        assertThat(saved.getFailureContext().failReason()).isEqualTo(FAIL_REASON);
        assertThat(saved.isFailure()).isTrue();
    }

    // ========== terminateSession ==========

    @Test
    void terminateSession_shouldUpdateAudit() {
        // Given
        LoginAudit audit = createOnlineAudit();
        when(auditRepository.findByTokenId(TOKEN_ID)).thenReturn(Optional.of(audit));

        // When
        auditAppService.terminateSession(TOKEN_ID, LogoutReason.LOGOUT);

        // Then
        verify(auditRepository, times(1)).update(audit);
        assertThat(audit.getStatus()).isEqualTo(OnlineStatus.OFFLINE);
        assertThat(audit.getLogoutReason()).isEqualTo(LogoutReason.LOGOUT);
    }

    @Test
    void terminateSession_shouldLogWarnWhenBizException() {
        // Given：token 不存在
        when(auditRepository.findByTokenId(TOKEN_ID)).thenReturn(Optional.empty());

        // When
        auditAppService.terminateSession(TOKEN_ID, LogoutReason.LOGOUT);

        // Then：不调用 update，只记录 WARN
        verify(auditRepository, never()).update(any());
        // 日志会打印 WARN
    }

    @Test
    void terminateSession_shouldLogWarnWhenAlreadyOffline() {
        // Given：已下线
        LoginAudit audit = createOnlineAudit();
        audit.terminate(LogoutReason.LOGOUT); // 先下线
        when(auditRepository.findByTokenId(TOKEN_ID)).thenReturn(Optional.of(audit));

        // When：再次调用 terminateSession
        auditAppService.terminateSession(TOKEN_ID, LogoutReason.KICK);

        // Then：不会再次更新（因为业务异常被捕获，但日志 WARN）
        verify(auditRepository, never()).update(any());
    }

    @Test
    void terminateSession_shouldLogErrorWhenSystemException() {
        // Given
        LoginAudit audit = createOnlineAudit();
        when(auditRepository.findByTokenId(TOKEN_ID)).thenReturn(Optional.of(audit));
        doThrow(new PersistenceException("DB error")).when(auditRepository).update(audit);

        // When
        auditAppService.terminateSession(TOKEN_ID, LogoutReason.LOGOUT);

        // Then：不向上抛异常，但打印 ERROR
        verify(auditRepository, times(1)).update(audit);
    }

    // ========== 辅助方法 ==========

    private LoginAudit createOnlineAudit() {
        return LoginAudit.recordSuccess(
            USER_ID,
            USERNAME,
            TOKEN_ID,
            LocalDateTime.now().plusHours(2),
            new DeviceInfo(DEVICE_TYPE, USER_AGENT),
            new IpAddress(IP)
        );
    }

}