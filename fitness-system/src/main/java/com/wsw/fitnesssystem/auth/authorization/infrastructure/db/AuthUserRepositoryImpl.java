package com.wsw.fitnesssystem.auth.authorization.infrastructure.db;

import com.wsw.fitnesssystem.auth.authentication.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.authentication.domain.port.AuthUserRepository;
import com.wsw.fitnesssystem.auth.authentication.infrastructure.persistence.db.converter.AuthUserConverter;
import com.wsw.fitnesssystem.auth.authorization.infrastructure.persistence.db.entity.SysUser;
import com.wsw.fitnesssystem.auth.authorization.infrastructure.persistence.db.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/16 11:33
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class AuthUserRepositoryImpl implements AuthUserRepository {

    private final SysUserMapper sysUserMapper;

    @Override
    public Optional<AuthUser> findByUsername(String username) {
        SysUser sysUser = sysUserMapper.selectByUsername(username);
        return Optional.ofNullable(AuthUserConverter.toDomain(sysUser));
    }

    @Override
    public boolean exists(long campusId, long userId) {
        return sysUserMapper.existsByCampusAndId(campusId, userId) > 0;
    }

}
