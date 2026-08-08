package com.wsw.fitnesssystem.auth.infrastructure.repository.db;

import com.wsw.fitnesssystem.auth.domain.model.UserInfo;
import com.wsw.fitnesssystem.auth.domain.port.UserInfoRepository;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.db.converter.UserInfoConverter;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.db.entity.SysUser;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.db.mapper.SysUserMapper;
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
