package com.wsw.fitnesssystem.auth.domain.model;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 认证用户聚合根
 * 只表达「和用户本身有关」的业务规则，不关心登录流程、不关心 JWT、不关心权限。
 *
 * @author loriyuhv
 * @version 1.0 2026/1/15 14:38
 * @since 1.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {
    private Long userId;
    private String username;
    private String password;
    private Integer status;

    public boolean isEnabled() {
        return Integer.valueOf(1).equals(status);
    }
}
