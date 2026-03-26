package com.wsw.fitnesssystem.auth.domain.port;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 12:16
 * @since 1.0
 */
public interface LoginFailRepository {
    int getFailCount(Long campusId, String username);

    void incrementFailCount(Long campusId, String username);

    void resetFailCount(Long campusId, String username);

    void lock(Long campusId, String username);

    boolean isLocked(Long campusId, String username);

    void unlock(Long campusId, String username);
}
