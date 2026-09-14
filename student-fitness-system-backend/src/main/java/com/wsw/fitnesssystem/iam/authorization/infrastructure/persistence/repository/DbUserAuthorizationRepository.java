package com.wsw.fitnesssystem.iam.authorization.infrastructure.persistence.repository;

import com.wsw.fitnesssystem.iam.authorization.domain.repository.UserAuthorizationRepository;
import com.wsw.fitnesssystem.iam.authorization.infrastructure.persistence.mapper.SysPermissionMapper;
import com.wsw.fitnesssystem.iam.authorization.infrastructure.persistence.mapper.SysRoleMapper;
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
public class DbUserAuthorizationRepository implements UserAuthorizationRepository {

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;

    @Override
    public Set<String> findRolesByUserIdAndCampusId(Long userId, Long campusId) {
        return roleMapper.selectRoleCodesByUserIdAndCampusId(userId, campusId);
    }

    @Override
    public Set<String> findPermissionsByUserIdAndCampusId(Long userId, Long campusId) {
        return permissionMapper.selectPermCodesByUserIdAndCampusId(userId, campusId);
    }

    @Override
    public Integer findMinDataScope(Long userId, Long campusId) {
        return roleMapper.selectMinDataScopeByUserIdAndCampusId(userId, campusId);
    }

}
