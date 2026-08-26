package com.wsw.fitnesssystem.auth.authorization.domain.port;

import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 18:37
 * @since 1.0
 */
public interface AuthorizationRepository {

    Set<String> findRolesByUserIdAndCampusId(Long userId, Long campusId);

    Set<String> findPermissionsByUserIdAndCampusId(Long userId, Long campusId);

}
