package com.wsw.fitnesssystem.auth.authentication.domain.service;

import com.wsw.fitnesssystem.auth.authentication.domain.model.AuthUser;

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
     * @return boolean
     */
    boolean userExists(long campusId, long userId);

}
