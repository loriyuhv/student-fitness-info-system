package com.wsw.fitnesssystem.shared.data_permission;

import net.sf.jsqlparser.expression.Expression;

/**
 * 数据权限 SQL 条件构造工具。
 * <p>根据 Context 生成 JSqlParser 表达式。</p>
 *
 * <p><b>待实现的细节：</b></p>
 * <ul>
 *   <li>解析 SQL，判断目标表是否有 user_id / campus_id / class_id 字段</li>
 *   <li>字段缺失时跳过（如查询体测规则表无需数据权限）</li>
 *   <li>data_scope=CLASS 且 allowedClassIds 为空时，返回永远为假的表达式（1=0）</li>
 *   <li>需要与业务表字段命名约定对齐（user_id/creator_id? class_id?）</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/10 12:53
 * @since 1.0
 */
public class DataPermissionSqlBuilder {

    static Expression build(Expression where, DataPermissionContext ctx) {
        // 骨架：实际实现需基于 JSqlParser 构造 AndExpression
        return switch (ctx.dataScope()) {
            case SELF -> appendEq(where, "user_id", ctx.currentUserId());
            case CLASS -> appendIn(where, "class_id", ctx.allowedClassIds());
            case COLLEGE -> appendEq(where, "campus_id", ctx.currentCampusId());
            case CUSTOM -> where;  // 预留：后续根据自定义规则解析
            default -> where;
        };
    }

    private static Expression appendEq(Expression where, String column, Object value) {
        // 伪代码：需用 JSqlParser 的 EqualsTo / Column 构造
        return where;
    }

    private static Expression appendIn(Expression where, String column, java.util.Set<Long> values) {
        // 伪代码：需用 JSqlParser 的 InExpression 构造
        return where;
    }

}
