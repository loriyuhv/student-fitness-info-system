package com.wsw.fitnesssystem.auth.application.service;

import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 12:41
 * @since 1.0
 */
public interface LoginFailLimitService {
    void check(Operator operator);

    int recordFail(Operator operator);

    void reset(Operator operator);

    void checkLock(Operator operator);

    void lock(Operator operator);

    void unlock(Operator operator);
}
