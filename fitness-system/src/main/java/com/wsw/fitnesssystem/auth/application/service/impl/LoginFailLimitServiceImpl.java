package com.wsw.fitnesssystem.auth.application.service.impl;

import com.wsw.fitnesssystem.auth.application.service.LoginFailLimitService;
import com.wsw.fitnesssystem.auth.domain.service.LoginFailLimitDomainService;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.redis.repository.RedisLoginFailRepository;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 12:42
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class LoginFailLimitServiceImpl implements LoginFailLimitService {
    private final RedisLoginFailRepository repository;
    private final LoginFailLimitDomainService domainService;
    private static final int MAX_FAIL_COUNT = 5;

    @Override
    public void check(Long campusId, String username) {
        int failCount = repository.getFailCount(campusId, username);
        domainService.checkFailCount(campusId, username, failCount, MAX_FAIL_COUNT);
    }

    @Override
    public int recordFail(Long campusId, String username) {
        repository.incrementFailCount(campusId, username);
        return repository.getFailCount(campusId, username);
    }

    @Override
    public void reset(Long campusId, String username) {
        repository.resetFailCount(campusId, username);
    }

    @Override
    public void checkLock(Long campusId, String username) {
        if (repository.isLocked(campusId, username)) {
            throw new BizException(ResultCode.ACCOUNT_LOCKED);
        }
    }

    @Override
    public void lock(Long campusId, String username) {
        repository.lock(campusId, username);
    }

    @Override
    public void unlock(Long campusId, String username) {
        repository.unlock(campusId, username);
    }
}
