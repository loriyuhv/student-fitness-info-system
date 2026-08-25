package com.wsw.fitnesssystem.auth.authentication.infrastructure.persistence.db.converter;

import com.wsw.fitnesssystem.auth.authentication.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.authorization.infrastructure.persistence.db.entity.SysUser;

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
                .userType(sysUser.getUserType())
                .status(sysUser.getStatus())
                .build();
    }
}
