package com.wsw.fitnesssystem.identity.infrastructure.persistence.repository;

import com.wsw.fitnesssystem.identity.domain.model.AuthUser;
import com.wsw.fitnesssystem.identity.domain.repository.AuthUserRepository;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.entity.SysUser;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.mapper.SysUserMapper;
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
        return Optional.of(AuthUser.from(sysUser));
    }
}
