package com.wsw.fitnesssystem.auth.session.domain.service.impl;

import com.wsw.fitnesssystem.auth.session.domain.policy.SessionLimitPolicy;
import com.wsw.fitnesssystem.auth.session.domain.port.SessionRepository;
import com.wsw.fitnesssystem.auth.session.domain.service.SessionDomainService;
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
    private final SessionLimitPolicy sessionLimitPolicy;

    @Override
    public void limitSessions(long campusId, long userId) {
        Long size = sessionRepository.countSessions(campusId, userId);
        int maxSessions = sessionLimitPolicy.getMaxSessions();
        if (size == null || size < maxSessions) return;

        sessionRepository.getOldestSession(campusId, userId)
            .ifPresent(oldest -> sessionRepository.removeSession(campusId, userId, oldest));
    }

}
