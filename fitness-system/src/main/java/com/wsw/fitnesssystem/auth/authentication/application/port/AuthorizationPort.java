package com.wsw.fitnesssystem.auth.authentication.application.port;

import com.wsw.fitnesssystem.auth.authentication.application.dto.port.AuthAuthorization;
import com.wsw.fitnesssystem.auth.authorization.application.dto.query.AuthorizationQuery;

/**
 * 授权端口（Authentication 模块定义）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>定义 Authentication 模块对授权能力的依赖契约</li>
 *   <li>包括加载权限和清除权限两个核心操作</li>
 *   <li>不依赖任何具体实现（本地/远程均透明）</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 18:42
 * @since 1.0
 */
public interface AuthorizationPort {

    /**
     * 获取用户授权信息（自动缓存）
     *
     * @return 用户授权信息（角色 + 权限）
     */
    AuthAuthorization getAuthorization(long userId, long campusId);

    /**
     * 移除用户授权缓存（踢人/权限变更时调用）
     *
     * @param userId   用户ID
     * @param campusId 校区ID
     */
    void removeAuthorization(long userId, long campusId);

}
