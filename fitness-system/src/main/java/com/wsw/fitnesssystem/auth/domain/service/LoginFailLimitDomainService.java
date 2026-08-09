package com.wsw.fitnesssystem.auth.domain.service;

import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 12:08
 * @since 1.0
 */
public interface LoginFailLimitDomainService {
    void checkFailCount(
            Operator operator,
            int failCount,
            int maxFailCount
    );
}
