package com.wsw.fitnesssystem.identity.domain.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.entity.SysUser;

/**
 * 业务用户
 *
 * @author loriyuhv
 * @version 1.0 2026/1/15 14:38
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {
    private Long userId;
    private String username;
    private String password;
    private Integer status;

    public boolean isEnabled() {
        return status != null && status == 1;
    }

    public static AuthUser from(SysUser sysUser) {
        AuthUser user = new AuthUser();
        user.userId = sysUser.getUserId();
        user.username = sysUser.getUsername();
        user.password = sysUser.getPassword();
        user.status = sysUser.getStatus();
        return user;
    }
}
