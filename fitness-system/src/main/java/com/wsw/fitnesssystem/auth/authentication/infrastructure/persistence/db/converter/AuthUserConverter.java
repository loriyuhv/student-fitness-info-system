package com.wsw.fitnesssystem.auth.authentication.infrastructure.persistence.db.converter;

import com.wsw.fitnesssystem.auth.authentication.domain.model.AuthUser;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.UserPo;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/5 10:51
 * @since 1.0
 */
public class AuthUserConverter {
    public static AuthUser toDomain(UserPo userPO) {

        if (userPO == null) {
            return null;
        }

        return AuthUser.builder()
                .userId(userPO.getUserId())
                .campusId(userPO.getCampusId())
                .username(userPO.getUsername())
                .password(userPO.getPassword())
                .userType(userPO.getUserType())
                .status(userPO.getStatus())
                .build();
    }
}
