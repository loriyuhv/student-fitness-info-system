package com.wsw.fitnesssystem.iam.authorization.domain.port;

import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 18:37
 * @since 1.0
 */
public interface AuthorizationRepository {

    Set<String> findRolesByUserIdAndCampusId(Long userId, Long campusId);

    Set<String> findPermissionsByUserIdAndCampusId(Long userId, Long campusId);

    /**
     * 查询用户所有有效角色中最宽泛的 data_scope。
     * <p>取值逻辑：取 {@code sys_role.data_scope} 的<b>最小值</b>
     * （0-全部数据权限最大，数字越小权限越大）。</p>
     * <p>若用户无任何启用角色，返回 {@code null}（由上层兜底为最小权限）。</p>
     *
     * @param userId   用户 ID
     * @param campusId 校区 ID
     * @return 最小 data_scope，若无角色返回 {@code null}
     */
    Integer findMinDataScope(Long userId, Long campusId);

}
