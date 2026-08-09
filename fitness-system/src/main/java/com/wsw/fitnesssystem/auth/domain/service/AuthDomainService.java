package com.wsw.fitnesssystem.auth.domain.service;

import com.wsw.fitnesssystem.auth.domain.model.AuthUser;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;

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
     * @param operator 操作对象
     * @return boolean
     */
    boolean userExists(Operator operator);
}
