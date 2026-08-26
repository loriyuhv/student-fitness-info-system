package com.wsw.fitnesssystem.auth.authorization.application.port;

import com.wsw.fitnesssystem.auth.authorization.application.dto.UserAuthorization;

/**
 * <p>Port: 应用层需要一个“权限缓存能力”，但不关心 Redis / Caffeine / DB。
 * 应用层需要缓存权限抽象表达，而不是要类似Redis具体实现</p>
 *
 * <p><b>授权缓存服务</b></p>
 * <p><b>职责：</b></p>
 * <ul>
 *     <li>缓存用户授权结果</li>
 *     <li>提供快速读取</li>
 *     <li>支持权限失效</li>
 * </ul>
 *
 * <p><b>不负责：</b></p>
 * <ul>
 *     <li>计算权限</li>
 *     <li>登录逻辑</li>
 * </ul>
 *
 * @author loriyuhv
  * @version 1.0 2026/1/16 14:10
 * @since 1.0
 */
public interface AuthorizationCacheService {

    /**
     * 缓存用户权限快照
     */
    void cache(long campusId, long userId, UserAuthorization authorization);

    /**
     * 获取用户权限快照
     */
    UserAuthorization get(long campusId, long userId);

    /**
     * 移除用户权限（权限变更 / 强制刷新）
     */
    void evict(long campusId, long userId);

}
