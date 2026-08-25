package com.wsw.fitnesssystem.auth.authorization.infrastructure.db;

import com.wsw.fitnesssystem.auth.authentication.domain.model.UserInfo;
import com.wsw.fitnesssystem.auth.authentication.domain.port.UserInfoRepository;
import com.wsw.fitnesssystem.auth.authentication.infrastructure.persistence.db.converter.UserInfoConverter;
import com.wsw.fitnesssystem.auth.authorization.infrastructure.persistence.db.entity.SysUser;
import com.wsw.fitnesssystem.auth.authorization.infrastructure.persistence.db.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/5 08:26
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class UserInfoRepositoryImpl implements UserInfoRepository {
    private final SysUserMapper sysUserMapper;

    @Override
    public UserInfo findById(Long userId, Long campusId) {
        SysUser sysUser = sysUserMapper.selectByUserIdAndCampusId(userId, campusId);
        return UserInfoConverter.toDomain(sysUser);
    }
}
