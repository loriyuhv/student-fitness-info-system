package com.wsw.fitnesssystem.auth.domain.service;

import com.wsw.fitnesssystem.auth.domain.model.AuthUser;

/**
 * 认证规则
 *
 * @author loriyuhv
 * @version 1.0 2026/1/15 14:41
 * @since 1.0
 */
public interface AuthDomainService {
    /**
     * 登录认证
     * @param username 用户名
     * @param rawPassword 用户密码（未加工）
     * @return 认证用户
     */
    AuthUser login(String username, String rawPassword);

    /**
     * 校验用户是否存在
     * @param campusId 校区ID
     * @param userId 用户ID
     * @return boolean
     */
    boolean userExists(Long campusId, Long userId);
}
