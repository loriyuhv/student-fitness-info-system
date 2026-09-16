package com.wsw.fitnesssystem.iam.authentication.domain.repository;

import com.wsw.fitnesssystem.iam.authentication.domain.model.AuthAccount;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/16 11:56
 * @since 1.0
 */
public interface AuthAccountRepository {

    AuthAccount save(AuthAccount account);

    Optional<AuthAccount> findByUserIdAndCampusId(Long userId, Long campusId);

    Optional<AuthAccount> findByUsername(String username);

    Set<String> findExistingUsernames(Collection<String> usernames);

    boolean existsByUsername(String username);

}
