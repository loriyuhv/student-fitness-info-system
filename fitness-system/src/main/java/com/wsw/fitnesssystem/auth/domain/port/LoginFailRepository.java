package com.wsw.fitnesssystem.auth.domain.port;

import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 12:16
 * @since 1.0
 */
public interface LoginFailRepository {
    int getFailCount(Operator operator);

    void incrementFailCount(Operator operator);

    void resetFailCount(Operator operator);

    void lock(Operator operator);

    boolean isLocked(Operator operator);

    void unlock(Operator operator);
}
