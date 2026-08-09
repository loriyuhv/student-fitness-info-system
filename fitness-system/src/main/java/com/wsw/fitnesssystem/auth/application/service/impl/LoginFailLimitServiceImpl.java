package com.wsw.fitnesssystem.auth.application.service.impl;

import com.wsw.fitnesssystem.auth.application.service.LoginFailLimitService;
import com.wsw.fitnesssystem.auth.domain.port.LoginFailRepository;
import com.wsw.fitnesssystem.auth.domain.service.LoginFailLimitDomainService;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
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
    private final LoginFailRepository repository;
    private final LoginFailLimitDomainService domainService;
    private static final int MAX_FAIL_COUNT = 5;

    @Override
    public void check(Operator operator) {
        int failCount = repository.getFailCount(operator);
        domainService.checkFailCount(operator, failCount, MAX_FAIL_COUNT);
    }

    @Override
    public int recordFail(Operator operator) {
        repository.incrementFailCount(operator);
        return repository.getFailCount(operator);
    }

    @Override
    public void reset(Operator operator) {
        repository.resetFailCount(operator);
    }

    @Override
    public void checkLock(Operator operator) {
        if (repository.isLocked(operator)) {
            throw new BizException(ResultCode.ACCOUNT_LOCKED);
        }
    }

    @Override
    public void lock(Operator operator) {
        repository.lock(operator);
    }

    @Override
    public void unlock(Operator operator) {
        repository.unlock(operator);
    }
}
