package com.wsw.fitnesssystem.identity.infrastructure.security.authorization;

import com.wsw.fitnesssystem.identity.application.authorization.UserAuthorization;

/**
 * 授权缓存服务
 * 职责：
 * - 缓存用户授权结果
 * - 提供快速读取
 * - 支持权限失效
 * 不负责：
 * - 计算权限
 * - 登录逻辑
 * @author loriyuhv
  * @version 1.0 2026/1/16 14:10
 * @since 1.0
 */
public interface AuthorizationCacheService {

    /**
     * 缓存用户权限快照
     */
    void cache(UserAuthorization authorization);

    /**
     * 获取用户权限快照
     */
    UserAuthorization get(Long userId);

    /**
     * 移除用户权限（权限变更 / 强制刷新）
     */
    void evict(Long userId);
}
