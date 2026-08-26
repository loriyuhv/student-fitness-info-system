package com.wsw.fitnesssystem.auth.authorization.application.service;

import com.wsw.fitnesssystem.auth.authorization.application.dto.query.AuthorizationQuery;
import com.wsw.fitnesssystem.auth.authorization.application.dto.result.UserAuthorization;

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
 * @version 1.0 2026/1/16 13:47
 * @since 1.0
 */
public interface AuthorizationQueryService {

    /**
     * 对用户进行授权，返回权限快照
     * @param authorizationQuery 查询参数
     * @return 用户权限快照
     */
    UserAuthorization authorize(AuthorizationQuery authorizationQuery);

    /**
     * 移除用户所有角色和权限
     */
    void removeAuthorization(long userId, long campusId);

}
