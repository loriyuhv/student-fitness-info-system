package com.wsw.fitnesssystem.auth.domain.service;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 12:08
 * @since 1.0
 */
public interface LoginFailLimitDomainService {
    void checkFailCount(
        Long campusId,
        String username,
        int failCount,
        int maxFailCount
    );
}
