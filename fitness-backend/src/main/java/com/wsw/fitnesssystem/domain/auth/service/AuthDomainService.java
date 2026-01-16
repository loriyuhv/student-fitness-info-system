package com.wsw.fitnesssystem.domain.auth.service;

import com.wsw.fitnesssystem.domain.auth.model.AuthUser;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/15 14:41
 * @since 1.0
 */
public interface AuthDomainService {
    /**
     * 根据用户名查询用户
     */
    AuthUser loadByUsername(String username);
}
