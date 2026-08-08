package com.wsw.fitnesssystem.auth.infrastructure.persistence.db.converter;

import com.wsw.fitnesssystem.auth.domain.model.UserInfo;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.db.entity.SysUser;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/5 08:33
 * @since 1.0
 */
public class UserInfoConverter {
    public static UserInfo toDomain(SysUser sysUser) {
        if (sysUser == null) {
            return null;
        }

        return UserInfo.builder()
                .userId(sysUser.getUserId())
                .campusId(sysUser.getCampusId())
                .username(sysUser.getUsername())
                .nickname(sysUser.getNickname())
                .phoneNumber(sysUser.getPhoneNumber())
                .email(sysUser.getEmail())
                .remark(sysUser.getRemark())
                .source(sysUser.getSource())
                .userType(sysUser.getUserType())
                .build();
    }
}
