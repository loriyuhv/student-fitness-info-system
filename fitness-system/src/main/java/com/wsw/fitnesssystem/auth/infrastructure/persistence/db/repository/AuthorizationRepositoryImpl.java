package com.wsw.fitnesssystem.auth.infrastructure.persistence.db.repository;

import com.wsw.fitnesssystem.auth.domain.port.AuthorizationRepository;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.db.mapper.SysPermissionMapper;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.db.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 18:39
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class AuthorizationRepositoryImpl implements AuthorizationRepository {
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;

    @Override
    public Set<String> findRolesByUserId(Long userId) {
        return roleMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    public Set<String> findPermissionsByUserId(Long userId) {
        return permissionMapper.selectPermCodesByUserId(userId);
    }
}
