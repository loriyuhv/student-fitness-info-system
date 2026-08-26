package com.wsw.fitnesssystem.auth.authorization.application.service.impl;

import com.wsw.fitnesssystem.auth.authorization.application.service.AuthorizationQueryService;
import com.wsw.fitnesssystem.auth.authorization.application.dto.query.AuthorizationQuery;
import com.wsw.fitnesssystem.auth.authorization.application.dto.result.UserAuthorization;
import com.wsw.fitnesssystem.auth.authorization.application.port.AuthorizationCacheService;
import com.wsw.fitnesssystem.auth.authorization.domain.port.AuthorizationRepository;
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
public class AuthorizationQueryServiceImpl implements AuthorizationQueryService {

    private final AuthorizationRepository authorizationRepository;
    private final AuthorizationCacheService cacheService;

    @Override
    public UserAuthorization authorize(AuthorizationQuery authorizationQuery) {

        Long campusId = authorizationQuery.getCampusId();
        Long userId = authorizationQuery.getUserId();

        // 1. 先查缓存
        UserAuthorization cached = cacheService.get(userId, campusId);
        if (cached != null) {
            log.info("权限缓存命中：{}:{}", userId, campusId);
            return cached;
        }

        // 2. 查DB
        // 一次性查询角色
        Set<String> roles = authorizationRepository.findRolesByUserIdAndCampusId(userId, campusId);

        // 一次性查询权限
        Set<String> permissions = authorizationRepository.findPermissionsByUserIdAndCampusId(userId, campusId);
        UserAuthorization fresh = UserAuthorization.builder().userId(userId).campusId(campusId).roles(roles)
            .permissions(permissions).build();

        // 3. 写缓存
        cacheService.cache(userId, campusId, fresh);
        log.info("权限缓存写入: {}:{}", userId, campusId);

        return fresh;
    }

    @Override
    public void removeAuthorization(long userId, long campusId) {
        cacheService.evict(userId, campusId);
    }

}
