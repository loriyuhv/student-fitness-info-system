package com.wsw.fitnesssystem.identity.domain.repository;

import com.wsw.fitnesssystem.identity.domain.model.AuthUser;

import java.util.Optional;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/16 11:31
 * @since 1.0
 */
public interface AuthUserRepository {
    Optional<AuthUser> findByUsername(String username);
}
