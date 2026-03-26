package com.wsw.fitnesssystem.auth.domain.service.impl;

import com.wsw.fitnesssystem.auth.domain.port.SessionRepository;
import com.wsw.fitnesssystem.auth.domain.service.SessionDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 10:59
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class SessionDomainServiceImpl implements SessionDomainService {
    private final SessionRepository sessionRepository;

    @Override
    public void limitSessions(Long campusId, Long userId, int maxSessions) {
        Long size = sessionRepository.countSessions(campusId, userId);
        if (size == null || size < maxSessions) return;

        sessionRepository.getOldestSession(campusId, userId)
            .ifPresent(oldest -> sessionRepository.removeSession(
                campusId, userId, oldest)
            );
    }
}
