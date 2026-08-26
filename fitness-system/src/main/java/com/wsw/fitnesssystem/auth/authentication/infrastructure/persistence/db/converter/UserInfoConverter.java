package com.wsw.fitnesssystem.auth.authentication.infrastructure.persistence.db.converter;

import com.wsw.fitnesssystem.auth.authentication.domain.model.UserInfo;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.UserPo;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/5 08:33
 * @since 1.0
 */
public class UserInfoConverter {
    public static UserInfo toDomain(UserPo userPO) {
        if (userPO == null) {
            return null;
        }

        return UserInfo.builder()
                .userId(userPO.getUserId())
                .campusId(userPO.getCampusId())
                .username(userPO.getUsername())
                .nickname(userPO.getNickname())
                .phoneNumber(userPO.getPhoneNumber())
                .email(userPO.getEmail())
                .remark(userPO.getRemark())
                .source(userPO.getSource())
                .userType(userPO.getUserType())
                .build();
    }
}
