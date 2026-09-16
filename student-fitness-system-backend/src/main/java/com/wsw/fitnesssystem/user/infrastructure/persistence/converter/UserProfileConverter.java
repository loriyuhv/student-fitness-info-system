package com.wsw.fitnesssystem.user.infrastructure.persistence.converter;

import com.wsw.fitnesssystem.user.domain.model.UserProfile;
import com.wsw.fitnesssystem.user.domain.vb.Gender;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.UserProfilePo;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 09:04
 * @since 1.0
 */
public final class UserProfileConverter {

    private UserProfileConverter() {}

    public static UserProfile toDomain(UserProfilePo po) {
        if (po == null) return null;
        return UserProfile.reconstitute(
            po.getProfileId(),
            po.getUserId(),
            po.getCampusId(),
            po.getNickname(),
            po.getPhoneNumber(),
            po.getEmail(),
            po.getGender() == null ? null : Gender.of(po.getGender()),
            po.getBirthDate(),
            po.getAvatarUrl(),
            po.getAddress(),
            po.getRemark(),
            po.getDeleted() != null && po.getDeleted() == 1,
            po.getCreateBy(),
            po.getCreateTime(),
            po.getUpdateBy(),
            po.getUpdateTime()
        );
    }

    public static UserProfilePo toPo(UserProfile profile) {
        if (profile == null) return null;
        UserProfilePo po = new UserProfilePo();
        po.setProfileId(profile.getProfileId());
        po.setUserId(profile.getUserId());
        po.setCampusId(profile.getCampusId());
        po.setNickname(profile.getNickname());
        po.setPhoneNumber(profile.getPhoneNumber());
        po.setEmail(profile.getEmail());
        po.setGender(profile.getGender().getCode());
        po.setBirthDate(profile.getBirthDate());
        po.setAvatarUrl(profile.getAvatarUrl());
        po.setAddress(profile.getAddress());
        po.setRemark(profile.getRemark());
        po.setDeleted(profile.isDeleted() ? 1 : 0);
        po.setCreateBy(profile.getCreateBy());
        po.setCreateTime(profile.getCreateTime());
        po.setUpdateBy(profile.getUpdateBy());
        po.setUpdateTime(profile.getUpdateTime());
        return po;
    }

}
