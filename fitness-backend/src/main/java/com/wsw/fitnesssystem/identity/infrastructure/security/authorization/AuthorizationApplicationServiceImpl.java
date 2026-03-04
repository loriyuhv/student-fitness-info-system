package com.wsw.fitnesssystem.identity.infrastructure.security.authorization;

import com.wsw.fitnesssystem.identity.application.authorization.AuthorizationApplicationService;
import com.wsw.fitnesssystem.identity.application.authorization.UserAuthorization;
import com.wsw.fitnesssystem.identity.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.mapper.SysPermissionMapper;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 授权服务实现（基础设施层）
 * 这里可以：
 * - 查 DB
 * - 查 Redis
 * - 远程权限中心
 *
 * @author loriyuhv
 * @version 1.0 2026/1/16 13:48
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class AuthorizationApplicationServiceImpl implements AuthorizationApplicationService {
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;

    @Override
    public UserAuthorization authorize(AuthUser authUser) {

        Long userId = authUser.getUserId();

        // 一次性查询角色
        Set<String> roles =
            roleMapper.selectRoleCodesByUserId(userId);

        // 一次性查询权限
        Set<String> permissions =
            permissionMapper.selectPermCodesByUserId(userId);

        return new UserAuthorization(userId, roles, permissions);
    }
}
