package com.wsw.fitnesssystem.shared.data_permission.application.builder;

import com.wsw.fitnesssystem.shared.data_permission.infrastructure.mybatis.CustomDataPermissionHandler;
import com.wsw.fitnesssystem.shared.data_permission.application.registry.DataPermissionColumnRegistry;
import com.wsw.fitnesssystem.shared.data_permission.domain.DataPermissionColumns;
import com.wsw.fitnesssystem.shared.data_permission.domain.DataPermissionContext;
import com.wsw.fitnesssystem.shared.exception.SystemException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限 SQL 条件构造器。
 *
 * <p><b>职责：</b>根据 {@link DataPermissionContext} 生成 JSqlParser 表达式，
 * 由 {@link CustomDataPermissionHandler} 追加到原 SQL 的 WHERE 中。</p>
 *
 * <p><b>支持的维度：</b></p>
 * <ul>
 *   <li>{@code SELF}：{@code {selfColumn} = currentUserId}</li>
 *   <li>{@code CLASS}：{@code {classColumn} IN (allowedClassIds)}；集合为空 → {@code 1=0}</li>
 *   <li>{@code COLLEGE}：{@code {campusColumn} = currentCampusId}</li>
 *   <li>{@code ALL}：不加条件</li>
 *   <li>{@code CUSTOM}：暂不支持 → 保守拒绝（{@code 1=0}）</li>
 * </ul>
 *
 * <p><b>实现方式说明：</b>通过 {@link CCJSqlParserUtil#parseCondExpression(String)}
 * 把条件字符串解析为 AST，而非手动构造 {@code InExpression} / {@code ExpressionList}。
 * 原因是 JSqlParser 4.x 与 5.x 的 AST 构造 API 不兼容，字符串解析跨版本稳定。
 * 由于 {@code column} 为代码内常量、{@code value} 均为 Long，
 * 不存在 SQL 注入风险。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/10 12:53
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataPermissionSqlBuilder {

    private final DataPermissionColumnRegistry columnRegistry;

    /**
     * 生成数据权限过滤表达式。
     *
     * @param where       原 WHERE 条件，可能为 null
     * @param ctx         数据权限上下文
     * @param targetTable 目标表名（不带别名）
     * @return 组合后的表达式；若无需过滤返回 {@code where}
     */
    public Expression build(Expression where, DataPermissionContext ctx, String targetTable) {
        if (ctx == null || ctx.isAllVisible()) {
            return where;
        }

        DataPermissionColumns columns = columnRegistry.find(targetTable);
        if (columns == null) {
            log.debug("Table [{}] not registered for data permission, skip", targetTable);
            return where;
        }

        Expression condition = switch (ctx.dataScope()) {
            case SELF -> buildSelf(columns, ctx);
            case CLASS -> buildClasses(columns, ctx);
            case COLLEGE -> buildCampus(columns, ctx);
            case CUSTOM -> buildDeny();
            default -> null;
        };

        if (condition == null) {
            return where;
        }
        return where == null ? condition : new AndExpression(where, condition);
    }

    // ==================== 各维度构造 ====================

    private Expression buildSelf(DataPermissionColumns columns, DataPermissionContext ctx) {
        // 表不支持 SELF → 跳过
        if (columns.selfColumn() == null) {
            return null;
        }

        // 表支持 SELF，但当前用户 ID 缺失 → 异常，拒绝
        if (ctx.currentUserId() == null) {
            log.warn("SELF scope but currentUserId is null, deny all");
            return buildDeny();
        }
        return parse(columns.selfColumn() + " = " + ctx.currentUserId());
    }

    private Expression buildClasses(DataPermissionColumns columns, DataPermissionContext ctx) {
        // 表不支持 CLASS 维度 → 跳过（返回 null，外层不加条件）
        if (columns.classColumn() == null) {
            log.debug("Table doesn't support CLASS scope, skip filter");
            return null;
        }

        // 表支持 CLASS，但教师无任教班级 → 拒绝
        Set<Long> classIds = ctx.allowedClassIds();
        if (classIds == null || classIds.isEmpty()) {
            log.warn("CLASS scope but no classes, deny all");
            return buildDeny();
        }

        // 正常情况
        String inClause = classIds.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
        return parse(columns.classColumn() + " IN (" + inClause + ")");
    }

    private Expression buildCampus(DataPermissionColumns columns, DataPermissionContext ctx) {
        if (columns.campusColumn() == null || ctx.currentCampusId() == null) {
            log.warn("COLLEGE scope requested but no campusColumn or currentCampusId, deny all");
            return buildDeny();
        }
        return parse(columns.campusColumn() + " = " + ctx.currentCampusId());
    }

    /**
     * 拒绝所有：{@code 1 = 0}。
     * <p>用于权限请求但无法满足时（如教师无任教班级、CUSTOM 未实现），
     * 保守拒绝优于默认放行。</p>
     */
    private Expression buildDeny() {
        return parse("1 = 0");
    }

    // ==================== 工具方法 ====================

    /**
     * 把条件字符串解析为 JSqlParser 表达式。
     * <p>用字符串而不是直接构造 AST，规避 JSqlParser 4.x / 5.x 的 API 差异。</p>
     *
     * @param condition 形如 {@code "user_id = 123"} 或 {@code "class_id IN (1,2,3)"}
     */
    private Expression parse(String condition) {
        try {
            return CCJSqlParserUtil.parseCondExpression(condition);
        } catch (JSQLParserException e) {
            log.error("Failed to parse data permission condition: {}", condition, e);
            throw new SystemException(ResultCode.SYSTEM_ERROR,
                "数据权限条件构造失败：" + condition, e);
        }
    }
}