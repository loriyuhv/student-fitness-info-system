package com.wsw.fitnesssystem.auth.infrastructure.persistence.converter;

import com.wsw.fitnesssystem.auth.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.entity.SysUser;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/5 10:51
 * @since 1.0
 */
public class AuthUserConverter {
    public static AuthUser toDomain(SysUser sysUser) {

        if (sysUser == null) {
            return null;
        }

        return AuthUser.builder()
            .userId(sysUser.getUserId())
            .campusId(sysUser.getCampusId())
            .username(sysUser.getUsername())
            .password(sysUser.getPassword())
            .status(sysUser.getStatus())
            .build();
    }
}
