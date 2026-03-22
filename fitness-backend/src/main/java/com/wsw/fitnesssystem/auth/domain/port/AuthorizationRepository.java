package com.wsw.fitnesssystem.auth.domain.port;

import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 18:37
 * @since 1.0
 */
public interface AuthorizationRepository {
    Set<String> findRolesByUserId(Long userId);

    Set<String> findPermissionsByUserId(Long userId);
}
