package com.wsw.fitnesssystem.shared.data_permission;

import java.util.Set;

/**
 * 数据权限上下文（不可变值对象）。
 * <p>由请求入口组装，在 ThreadLocal 中传递，供 MyBatis 拦截器读取。</p>
 * <p>包含本次请求的过滤规则，避免在 SQL 解析时反复查询数据库。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/10 11:54
 * @since 1.0
 */
public record DataPermissionContext(
    DataScope dataScope,       // 数据权限范围
    Long currentUserId,        // 当前用户 ID（用于 data_scope=1）
    Long currentCampusId,      // 当前校区 ID（用于 data_scope=3）
    Set<Long> allowedClassIds  // 允许访问的班级 ID（用于 data_scope=2）
) {

    /** 是否全部数据可见（无需追加条件） */
    public boolean isAllVisible() {
        return dataScope == DataScope.ALL;
    }

}
