package com.wsw.fitnesssystem.user.infrastructure.persistence.converter;

import com.wsw.fitnesssystem.user.domain.model.UserProfile;
import com.wsw.fitnesssystem.user.domain.valueobject.Gender;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.UserProfilePo;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 09:04
 * @since 1.0
 */
@Component
public class UserProfileConverter {

    public UserProfilePo toPo(UserProfile profile) {
        UserProfilePo po = new UserProfilePo();
        po.setProfileId(profile.getProfileId());
        po.setUserId(profile.getUserId());
        po.setCampusId(profile.getCampusId());
        po.setGender(profile.getGender().getCode());
        po.setBirthDate(profile.getBirthDate());
        po.setAvatarUrl(profile.getAvatarUrl());
        po.setAddress(profile.getAddress());
        po.setLastLoginIp(profile.getLastLoginIp());
        po.setLastLoginTime(profile.getLastLoginTime());
        po.setDeleted(profile.isDeleted() ? 1 : 0);
        return po;
    }

    public UserProfile toDomain(UserProfilePo po) {
        return UserProfile.builder()
            .profileId(po.getProfileId())
            .userId(po.getUserId())
            .campusId(po.getCampusId())
            .gender(Gender.of(po.getGender()))
            .birthDate(po.getBirthDate())
            .avatarUrl(po.getAvatarUrl())
            .address(po.getAddress())
            .lastLoginIp(po.getLastLoginIp())
            .lastLoginTime(po.getLastLoginTime())
            .deleted(po.getDeleted() == 1)
            .build();
    }

}
