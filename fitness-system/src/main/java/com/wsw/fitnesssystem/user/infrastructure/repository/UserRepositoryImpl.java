package com.wsw.fitnesssystem.user.infrastructure.repository;

import com.wsw.fitnesssystem.user.domain.model.User;
import com.wsw.fitnesssystem.user.domain.port.UserRepository;
import com.wsw.fitnesssystem.user.domain.valueobject.UserSource;
import com.wsw.fitnesssystem.user.domain.valueobject.UserStatus;
import com.wsw.fitnesssystem.user.domain.valueobject.UserType;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.UserPo;
import com.wsw.fitnesssystem.user.infrastructure.persistence.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/26 13:58
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final SysUserMapper userMapper;

    @Override
    public Optional<User> findByCampusIdAndUserId(Long campusId, Long userId) {
        UserPo po = userMapper.selectByCampusIdAndUserId(campusId, userId);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        UserPo po = userMapper.selectByUsername(username);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    private User toDomain(UserPo userPo) {
        return User.builder()
            .userId(userPo.getUserId())
            .campusId(userPo.getCampusId())
            .username(userPo.getUsername())
            .password(userPo.getPassword())
            .nickname(userPo.getNickname())
            .phoneNumber(userPo.getPhoneNumber())
            .email(userPo.getEmail())
            .remark(userPo.getRemark())
            .userType(UserType.of(userPo.getUserType()))
            .source(UserSource.of(userPo.getSource()))
            .status(UserStatus.of(userPo.getStatus()))
            .build();
    }

}
