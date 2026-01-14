package com.wsw.fitnesssystem.infrastructure.persistence.repository;

import com.wsw.fitnesssystem.domain.user.User;
import com.wsw.fitnesssystem.domain.user.UserRepository;
import com.wsw.fitnesssystem.infrastructure.persistence.entity.SysUser;
import com.wsw.fitnesssystem.infrastructure.persistence.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:48
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final SysUserMapper sysUserMapper;

    @Override
    public Optional<User> findByUsername(String username) {
        SysUser sysUser = sysUserMapper.selectByUsername(username);
        if (sysUser == null) {
            return Optional.empty();
        }

        return Optional.of(toDomain(sysUser));
    }

    @Override
    public Set<String> findPermissions(Long userId) {
        return sysUserMapper.selectPermissionsByUserId(userId);
    }

    private User toDomain(SysUser sysUser) {
        return new User(
            sysUser.getUserId(),
            sysUser.getUsername(),
            sysUser.getPassword(),
            sysUser.getStatus()
        );
    }
}
