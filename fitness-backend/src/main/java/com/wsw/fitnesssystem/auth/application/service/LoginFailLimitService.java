package com.wsw.fitnesssystem.auth.application.service;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 12:41
 * @since 1.0
 */
public interface LoginFailLimitService {
    void check(Long campusId, String username);

    int recordFail(Long campusId, String username);

    void reset(Long campusId, String username);

    void checkLock(Long campusId, String username);

    void lock(Long campusId, String username);

    void unlock(Long campusId, String username);
}
