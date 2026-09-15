package com.wsw.fitnesssystem.shared.data_permission.application.registry;

import com.wsw.fitnesssystem.shared.data_permission.application.builder.DataPermissionSqlBuilder;
import com.wsw.fitnesssystem.shared.data_permission.domain.DataPermissionColumns;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 数据权限字段映射注册表。
 *
 * <p><b>职责：</b>维护"表名 → 字段映射"的关系，供 {@link DataPermissionSqlBuilder} 查询。</p>
 * <p><b>扩展方式：</b>新增需要数据权限的表时，在 {@code TABLE_COLUMNS} 追加一行即可。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/15 07:02
 * @since 1.0
 */
@Component
public class DataPermissionColumnRegistry {

    private static final Map<String, DataPermissionColumns> TABLE_COLUMNS = Map.ofEntries(
        // 学生档案：user_id 用于 SELF，class_id 用于 CLASS，campus_id 用于租户隔离
        Map.entry("student_profile", new DataPermissionColumns("user_id", "class_id", "campus_id")),

        // 用户档案：只有 user_id 和 campus_id
        Map.entry("user_profile", new DataPermissionColumns("user_id", null, "campus_id")),

        // 教师档案：只有 user_id 和 campus_id
        Map.entry("teacher_profile", new DataPermissionColumns("user_id", null, "campus_id")),

        // 系统用户：只有 user_id 和 campus_id
        Map.entry("sys_user", new DataPermissionColumns("user_id", null, "campus_id"))
    );

    /**
     * 查询表的数据权限字段映射。
     *
     * @param tableName 表名（不区分大小写）
     * @return 字段映射；未注册的表返回 {@code null}（表示不加数据权限过滤）
     */
    public DataPermissionColumns find(String tableName) {
        if (tableName == null) {
            return null;
        }
        return TABLE_COLUMNS.get(tableName.toLowerCase());
    }

}
