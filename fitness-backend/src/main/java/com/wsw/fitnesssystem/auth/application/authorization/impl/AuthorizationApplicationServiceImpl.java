package com.wsw.fitnesssystem.auth.application.authorization.impl;

import com.wsw.fitnesssystem.auth.application.authorization.AuthorizationApplicationService;
import com.wsw.fitnesssystem.auth.application.authorization.dto.UserAuthorization;
import com.wsw.fitnesssystem.auth.application.port.AuthorizationCacheService;
import com.wsw.fitnesssystem.auth.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.mapper.SysPermissionMapper;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationApplicationServiceImpl implements AuthorizationApplicationService {
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final AuthorizationCacheService cacheService;

    @Override
    public UserAuthorization authorize(AuthUser authUser) {

        Long userId = authUser.getUserId();

        // 1. 先查缓存
        UserAuthorization cached = cacheService.get(userId);
        if (cached != null) {
            log.info("权限缓存命中：{}", userId);
            return cached;
        }

        // 2. 查DB
        // 一次性查询角色
        Set<String> roles =
            roleMapper.selectRoleCodesByUserId(userId);

        // 一次性查询权限
        Set<String> permissions =
            permissionMapper.selectPermCodesByUserId(userId);
        UserAuthorization fresh = new UserAuthorization(userId, roles, permissions);

        // 3. 写缓存
        cacheService.cache(fresh);
        log.info("权限缓存写入: {}", userId);

        return fresh;
    }
}
