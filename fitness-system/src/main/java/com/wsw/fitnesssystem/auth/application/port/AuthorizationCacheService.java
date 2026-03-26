package com.wsw.fitnesssystem.auth.application.port;

import com.wsw.fitnesssystem.auth.application.authorization.dto.UserAuthorization;

/**
 * Port: 应用层需要一个“权限缓存能力”，但不关心 Redis / Caffeine / DB。
 * 应用层需要缓存权限抽象表达，而不是要类似Redis具体实现
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
    void cache(Long campusId, UserAuthorization authorization);

    /**
     * 获取用户权限快照
     */
    UserAuthorization get(Long campusId, Long userId);

    /**
     * 移除用户权限（权限变更 / 强制刷新）
     */
    void evict(Long campusId, Long userId);
}
