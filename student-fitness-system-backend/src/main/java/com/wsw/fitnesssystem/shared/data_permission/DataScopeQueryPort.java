package com.wsw.fitnesssystem.shared.data_permission;

/**
 * 数据权限规则查询端口。
 * <p>由 iam.authorization 模块实现，返回用户的 data_scope。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/10 12:48
 * @since 1.0
 */
public interface DataScopeQueryPort {

    /**
     * 查询用户的最大数据权限范围（多角色时取最宽泛的）。
     *
     * @param userId   用户 ID
     * @param campusId 校区 ID
     * @return 数据权限范围
     */
    DataScope queryMaxDataScope(Long userId, Long campusId);

}
