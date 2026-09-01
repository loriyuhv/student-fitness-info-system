package com.wsw.fitnesssystem.user.infrastructure.persistence.converter;

import com.wsw.fitnesssystem.user.domain.model.User;
import com.wsw.fitnesssystem.user.domain.valueobject.UserSource;
import com.wsw.fitnesssystem.user.domain.valueobject.UserStatus;
import com.wsw.fitnesssystem.user.domain.valueobject.UserType;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.UserPo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/1 15:51
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class UserConverter {

    private final PasswordEncoder passwordEncoder;

    /**
     * 领域模型 → 持久化实体
     */
    public UserPo toPo(User user) {
        UserPo po = new UserPo();
        po.setCampusId(user.getCampusId());
        po.setUsername(user.getUsername());
        // 加密密码
        po.setPassword(passwordEncoder.encode(user.getPassword()));
        po.setNickname(user.getNickname());
        po.setPhoneNumber(user.getPhoneNumber());
        po.setEmail(user.getEmail());
        po.setUserType(user.getUserType().getCode());
        po.setStatus(user.getStatus().getCode());
        // source 默认 IMPORT
        po.setSource(UserSource.IMPORT.getCode());
        // 逻辑删除默认 0
        po.setDeleted(0);
        return po;
    }

    /**
     * 持久化实体 → 领域模型
     */
    public User toDomain(UserPo po) {
        return User.builder()
            .userId(po.getUserId())
            .campusId(po.getCampusId())
            .username(po.getUsername())
            .password(po.getPassword())
            .nickname(po.getNickname())
            .phoneNumber(po.getPhoneNumber())
            .email(po.getEmail())
            .remark(po.getRemark())
            .userType(UserType.of(po.getUserType()))
            .source(UserSource.of(po.getSource()))
            .status(UserStatus.of(po.getStatus()))
            .deleted(po.getDeleted() == 1)
            .build();
    }

}
