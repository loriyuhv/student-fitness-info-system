package com.wsw.fitnesssystem.iam.authorization.application.service;

import com.wsw.fitnesssystem.iam.authorization.application.dto.query.AuthorizationQuery;
import com.wsw.fitnesssystem.iam.authorization.application.dto.result.UserAuthorization;
import com.wsw.fitnesssystem.iam.authorization.application.port.output.AuthorizationCachePort;
import com.wsw.fitnesssystem.iam.authorization.domain.repository.UserAuthorizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 用户授权查询服务
 * 职责：一次性计算“用户拥有什么权限”
 * 1. 根据用户身份查询权限快照
 * 2. 屏蔽权限数据来源
 *    （Redis、数据库、远程权限中心）
 * 不负责：
 * 1. 权限规则计算
 * 2. 角色继承
 * 3. 权限策略判断
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 15:56
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationQueryService {

    private final UserAuthorizationRepository userAuthorizationRepository;
    private final AuthorizationCachePort cacheService;

    /**
     * 对用户进行授权，返回权限快照
     * @param authorizationQuery 查询参数
     * @return 用户权限快照
     */
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
        Set<String> roles = userAuthorizationRepository.findRolesByUserIdAndCampusId(userId, campusId);

        // 一次性查询权限
        Set<String> permissions = userAuthorizationRepository.findPermissionsByUserIdAndCampusId(userId, campusId);
        UserAuthorization fresh = UserAuthorization.builder().userId(userId).campusId(campusId).roles(roles)
            .permissions(permissions).build();

        // 3. 写缓存
        cacheService.cache(userId, campusId, fresh);
        log.info("权限缓存写入: {}:{}", userId, campusId);

        return fresh;
    }


    /**
     * 移除用户所有角色和权限
     */
    public void removeAuthorization(long userId, long campusId) {
        cacheService.evict(userId, campusId);
    }

}
