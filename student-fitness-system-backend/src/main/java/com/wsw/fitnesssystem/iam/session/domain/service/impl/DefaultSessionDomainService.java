package com.wsw.fitnesssystem.iam.session.domain.service.impl;

import com.wsw.fitnesssystem.iam.session.domain.policy.SessionLimitPolicy;
import com.wsw.fitnesssystem.iam.session.domain.repository.SessionRepository;
import com.wsw.fitnesssystem.iam.session.domain.service.SessionDomainService;
import lombok.RequiredArgsConstructor;

/**
 * {@link SessionDomainService} 默认实现
 *
 * <p><b>说明：</b>本类属于领域层，不携带任何 Spring 注解，
 * 由基础设施层通过 {@code SessionDomainConfiguration} 显式注册为 Bean。</p>
 *
 * <p><b>策略：</b>超过上限时踢掉最早登录的会话（FIFO）。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/3/21 10:59
 * @since 1.0
 */
@RequiredArgsConstructor
public class DefaultSessionDomainService implements SessionDomainService {

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
