package com.wsw.fitnesssystem.user.application.port.output;

import com.wsw.fitnesssystem.user.application.dto.result.UserAuthorizationResult;

/**
 * 用户授权查询端口。
 *
 * <p><b>契约归属：</b>由 user 模块定义，authorization 模块实现。</p>
 * <p><b>用途：</b>查询指定用户的角色与权限编码集合。</p>
 *
 * <p><b>演进：</b>本地阶段由 {@code UserAuthorizationQueryLocalAdapter} 实现，
 * 微服务阶段由 {@code UserAuthorizationQueryFeignAdapter} 实现，调用方零改动。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 15:36
 * @since 1.0
 */
public interface UserAuthorizationQueryPort {

    /**
     * 查询用户在指定校区下的角色与权限。
     *
     * @param userId   用户 ID
     * @param campusId 校区 ID
     * @return 授权结果；若用户无任何角色或权限，返回空集合（非 null）
     */
    UserAuthorizationResult findByUserIdAndCampusId(Long userId, Long campusId);

}
