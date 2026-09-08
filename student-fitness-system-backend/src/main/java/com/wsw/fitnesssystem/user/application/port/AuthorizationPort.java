package com.wsw.fitnesssystem.user.application.port;

import com.wsw.fitnesssystem.user.application.dto.port.UserAuthorizationInfo;

/**
 * 授权信息查询端口（由用户模块定义，由适配器实现）
 *
 * @author loriyuhv
 * @version 1.0 2026/9/2 00:24
 * @since 1.0
 */
public interface AuthorizationPort {

    /**
     * 获取用户的角色和权限编码
     * @param userId 用户ID
     * @param campusId 校区ID
     * @return 角色集合和权限集合
     */
    UserAuthorizationInfo getAuthorizations(Long userId, Long campusId);

}
