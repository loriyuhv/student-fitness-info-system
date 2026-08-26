package com.wsw.fitnesssystem.auth.session.domain.service;

import com.wsw.fitnesssystem.auth.session.domain.policy.SessionLimitPolicy;
import com.wsw.fitnesssystem.auth.session.domain.port.SessionRepository;
import com.wsw.fitnesssystem.auth.session.domain.service.impl.SessionDomainServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/27 07:16
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class SessionDomainServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionLimitPolicy sessionLimitPolicy;

    @InjectMocks
    private SessionDomainServiceImpl sessionDomainService;

    // ========== 场景 1：在线会话数未达到上限，不应踢出任何会话 ==========

    @Test
    void shouldNotKickWhenSessionCountBelowLimit() {
        // Given：允许最大 3 个会话，当前只有 2 个在线
        when(sessionLimitPolicy.getMaxSessions()).thenReturn(3);
        when(sessionRepository.countSessions(1101L, 100L)).thenReturn(2L);

        // When
        sessionDomainService.limitSessions(1101L, 100L);

        // Then：没有调用 removeSession
        verify(sessionRepository, never()).removeSession(anyLong(), anyLong(), anyString());
        verify(sessionRepository, never()).getOldestSession(anyLong(), anyLong());
    }

    // ========== 场景 2：在线会话数正好达到上限，也不应踢出 ==========

    @Test
    void shouldNotKickWhenSessionCountEqualsLimit() {
        // Given：允许最大 3 个会话，当前正好 3 个在线
        when(sessionLimitPolicy.getMaxSessions()).thenReturn(3);
        when(sessionRepository.countSessions(1L, 100L)).thenReturn(3L);

        // When
        sessionDomainService.limitSessions(1L, 100L);

        // Then：没有调用 removeSession
        verify(sessionRepository, never()).removeSession(anyLong(), anyLong(), anyString());
        verify(sessionRepository, never()).getOldestSession(anyLong(), anyLong());
    }

    // ========== 场景 3：超限，应踢出最早登录的那个会话 ==========

    @Test
    void shouldKickOldestSessionWhenExceedsLimit() {
        // Given：允许最大 2 个会话，当前有 3 个在线
        when(sessionLimitPolicy.getMaxSessions()).thenReturn(2);
        when(sessionRepository.countSessions(1L, 100L)).thenReturn(3L);
        when(sessionRepository.getOldestSession(1L, 100L))
            .thenReturn(Optional.of("oldest-session-id"));

        // When
        sessionDomainService.limitSessions(1L, 100L);

        // Then：调用了 removeSession 且传入的是 "oldest-session-id"
        verify(sessionRepository).removeSession(1L, 100L, "oldest-session-id");
    }

    // ========== 场景 4：超限但获取最旧会话失败（理论上不会发生，但处理空值） ==========

    @Test
    void shouldNotKickWhenGetOldestSessionEmpty() {
        // Given：允许最大 1 个会话，当前有 2 个在线
        when(sessionLimitPolicy.getMaxSessions()).thenReturn(1);
        when(sessionRepository.countSessions(1L, 100L)).thenReturn(2L);
        when(sessionRepository.getOldestSession(1L, 100L))
            .thenReturn(Optional.empty()); // 缓存不一致，getOldest 返回空

        // When
        sessionDomainService.limitSessions(1L, 100L);

        // Then：由于获取不到最旧会话，removeSession 不会被调用
        verify(sessionRepository, never()).removeSession(anyLong(), anyLong(), anyString());
    }

}