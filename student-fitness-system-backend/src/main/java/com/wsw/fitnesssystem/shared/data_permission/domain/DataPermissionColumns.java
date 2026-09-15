package com.wsw.fitnesssystem.shared.data_permission.domain;

/**
 * 表的数据权限字段映射。
 *
 * <p><b>职责：</b>声明一张表中，用于数据权限过滤的三个字段名。
 * 为 {@code null} 表示该表不支持对应的权限维度（跳过该维度过滤）。</p>
 *
 * <p><b>字段约定：</b></p>
 * <ul>
 *   <li>{@code selfColumn}：SELF 范围过滤字段（如 student_profile.user_id）</li>
 *   <li>{@code classColumn}：CLASS 范围过滤字段（如 student_profile.class_id）</li>
 *   <li>{@code campusColumn}：COLLEGE 范围过滤字段（如 student_profile.campus_id）</li>
 * </ul>
 *
 * @param selfColumn   SELF 过滤字段名，null 表示不支持
 * @param classColumn  CLASS 过滤字段名，null 表示不支持
 * @param campusColumn COLLEGE / 租户隔离字段名，null 表示不支持
 * @author loriyuhv
 * @version 1.0 2026/9/15 07:00
 * @since 1.0
 */
public record DataPermissionColumns(
    String selfColumn,
    String classColumn,
    String campusColumn
) {
}
