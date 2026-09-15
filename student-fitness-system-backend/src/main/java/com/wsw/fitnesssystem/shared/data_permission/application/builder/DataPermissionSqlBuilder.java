package com.wsw.fitnesssystem.shared.data_permission.application.builder;

import com.wsw.fitnesssystem.shared.data_permission.application.registry.DataPermissionColumnRegistry;
import com.wsw.fitnesssystem.shared.data_permission.domain.DataPermissionColumns;
import com.wsw.fitnesssystem.shared.data_permission.domain.DataPermissionContext;
import com.wsw.fitnesssystem.shared.data_permission.domain.DataScope;
import com.wsw.fitnesssystem.shared.data_permission.infrastructure.mybatis.CustomDataPermissionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 数据权限 SQL 条件构造器。
 *
 * <p><b>职责：</b>根据 {@link DataPermissionContext} 生成 JSqlParser 的 {@link Expression}，
 * 由 {@link CustomDataPermissionHandler} 追加到原 SQL 的 WHERE 子句中。</p>
 *
 * <p><b>支持的权限维度：</b></p>
 * <table border="1">
 *   <caption>数据权限维度与 SQL 条件对照</caption>
 *   <tr><th>维度</th><th>生成条件</th><th>缺省行为</th></tr>
 *   <tr>
 *     <td>{@link DataScope#SELF SELF}</td>
 *     <td>{@code selfColumn = currentUserId}</td>
 *     <td>currentUserId 缺失 → 拒绝（{@code 1 = 0}）</td>
 *   </tr>
 *   <tr>
 *     <td>{@link DataScope#CLASS CLASS}</td>
 *     <td>{@code classColumn IN (allowedClassIds)}</td>
 *     <td>集合为空 → 拒绝（{@code 1 = 0}）</td>
 *   </tr>
 *   <tr>
 *     <td>{@link DataScope#COLLEGE COLLEGE}</td>
 *     <td>{@code campusColumn = currentCampusId}</td>
 *     <td>字段或值缺失 → 拒绝（{@code 1 = 0}）</td>
 *   </tr>
 *   <tr>
 *     <td>{@link DataScope#ALL ALL}</td>
 *     <td>不加条件</td>
 *     <td>—</td>
 *   </tr>
 *   <tr>
 *     <td>{@link DataScope#CUSTOM CUSTOM}</td>
 *     <td>暂未实现</td>
 *     <td>保守拒绝（{@code 1 = 0}）</td>
 *   </tr>
 * </table>
 *
 * <p><b>实现说明（重要）：</b></p>
 * <ul>
 *   <li><b>直接构造 AST：</b>使用 JSqlParser 5.x 提供的 {@link EqualsTo}、{@link InExpression}、
 *       {@link ExpressionList} 等对象直接构造表达式，不再采用"拼字符串 +
 *       {@code CCJSqlParserUtil.parseCondExpression}"的迂回方案。</li>
 *   <li><b>类型安全：</b>列名通过 {@link Column} 对象承载，值通过 {@link LongValue} 承载，
 *       编译期即可发现拼写错误，无需等到运行时解析失败。</li>
 *   <li><b>无 SQL 注入：</b>值统一封装为 {@code LongValue}，不再进入字符串拼接上下文，
 *       从类型层面消除注入风险。</li>
 *   <li><b>与 JSqlParser 5.x 对齐：</b>{@code ExpressionList} 在 5.x 中直接继承
 *       {@code ArrayList<Expression>}，本节通过其集合语义直接添加元素。</li>
 * </ul>
 *
 * <p><b>缺省策略：</b>当"用户请求了某个数据权限维度，但该维度所依赖的字段或数据缺失"时，
 * 采用<b>拒绝</b>（{@code 1 = 0}）而非静默放行。原因：静默放行意味着数据越权，
 * 属于安全红线；而拒绝只是查询不到数据，属于可用性问题，可被监控发现并修复。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/10 12:53
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataPermissionSqlBuilder {

    /**
     * 数据权限字段注册表。
     * <p>负责根据表名返回该表在 SELF / CLASS / COLLEGE 三个维度上对应的列名。</p>
     */
    private final DataPermissionColumnRegistry columnRegistry;

    // ==================== 对外入口 ====================

    /**
     * 生成数据权限过滤表达式，并与原 WHERE 条件组合。
     *
     * <p><b>组合规则：</b></p>
     * <ul>
     *   <li>原 {@code where} 为 null：直接返回权限条件；</li>
     *   <li>原 {@code where} 非 null：用 {@link AndExpression} 与权限条件做 {@code AND} 合并。</li>
     * </ul>
     *
     * <p><b>快速返回场景（不追加任何过滤）：</b></p>
     * <ul>
     *   <li>{@code ctx} 为 null（未登录 / 系统任务）；</li>
     *   <li>{@code ctx.isAllVisible()} 为 true（{@code data_scope = ALL}）；</li>
     *   <li>目标表未在 {@link DataPermissionColumnRegistry} 中登记。</li>
     * </ul>
     *
     * @param where       原 WHERE 条件，可能为 {@code null}
     * @param ctx         数据权限上下文，可能为 {@code null}
     * @param targetTable 目标表名（不带别名），用于查表对应的字段映射
     * @return 组合后的表达式；无需过滤时原样返回 {@code where}
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

    // ==================== 各维度条件构造 ====================

    /**
     * 构造 SELF 维度条件：{@code selfColumn = currentUserId}。
     *
     * <p><b>处理分支：</b></p>
     * <ul>
     *   <li>表未声明 selfColumn（字段为 {@code null}）→ 返回 {@code null}，外层不追加条件；</li>
     *   <li>currentUserId 缺失 → 记录 WARN 并返回拒绝条件，防止越权；</li>
     *   <li>正常情况 → 构造 {@link EqualsTo} 表达式。</li>
     * </ul>
     *
     * @param columns 目标表的数据权限字段映射
     * @param ctx     数据权限上下文
     * @return SELF 维度条件；表不支持时返回 {@code null}
     */
    private Expression buildSelf(DataPermissionColumns columns, DataPermissionContext ctx) {
        if (columns.selfColumn() == null) {
            log.debug("Table doesn't support SELF scope, skip filter");
            return null;
        }
        if (ctx.currentUserId() == null) {
            log.warn("SELF scope but currentUserId is null, deny all");
            return buildDeny();
        }
        return equals(columns.selfColumn(), ctx.currentUserId());
    }

    /**
     * 构造 CLASS 维度条件：{@code classColumn IN (allowedClassIds)}。
     *
     * <p><b>处理分支：</b></p>
     * <ul>
     *   <li>表未声明 classColumn → 返回 {@code null}，外层不追加条件；</li>
     *   <li>allowedClassIds 为 null 或空集 → 记录 WARN 并返回拒绝条件（教师无任教班级）；</li>
     *   <li>正常情况 → 构造 {@link InExpression} 表达式。</li>
     * </ul>
     *
     * @param columns 目标表的数据权限字段映射
     * @param ctx     数据权限上下文
     * @return CLASS 维度条件；表不支持时返回 {@code null}
     */
    private Expression buildClasses(DataPermissionColumns columns, DataPermissionContext ctx) {
        if (columns.classColumn() == null) {
            log.debug("Table doesn't support CLASS scope, skip filter");
            return null;
        }
        Set<Long> classIds = ctx.allowedClassIds();
        if (classIds == null || classIds.isEmpty()) {
            log.warn("CLASS scope but no classes, deny all");
            return buildDeny();
        }
        return in(columns.classColumn(), classIds);
    }

    /**
     * 构造 COLLEGE 维度条件：{@code campusColumn = currentCampusId}。
     *
     * <p><b>处理分支：</b></p>
     * <ul>
     *   <li>表未声明 campusColumn，或当前用户无 campusId → 记录 WARN 并返回拒绝条件；</li>
     *   <li>正常情况 → 构造 {@link EqualsTo} 表达式。</li>
     * </ul>
     *
     * @param columns 目标表的数据权限字段映射
     * @param ctx     数据权限上下文
     * @return COLLEGE 维度条件
     */
    private Expression buildCampus(DataPermissionColumns columns, DataPermissionContext ctx) {
        if (columns.campusColumn() == null) {
            log.debug("Table doesn't support COLLEGE scope, skip filter");
            return null;
        }
        if (ctx.currentCampusId() == null) {
            log.warn("COLLEGE scope but currentCampusId is null, deny all");
            return buildDeny();
        }
        return equals(columns.campusColumn(), ctx.currentCampusId());
    }

    // ==================== 通用表达式构造 ====================

    /**
     * 构造恒假条件 {@code 1 = 0}，用于"请求了权限但无法满足"的保守拒绝场景。
     *
     * <p><b>设计意图：</b>当请求了 SELF/CLASS/COLLEGE/CUSTOM 维度却缺少必要数据时，
     * 宁可查询结果为空，也不能让原 SQL 无过滤地执行。前者是可观测、可修复的可用性问题；
     * 后者是数据越权，属于安全红线。</p>
     *
     * @return 恒假的 {@link EqualsTo} 表达式
     */
    private Expression buildDeny() {
        EqualsTo deny = new EqualsTo();
        deny.setLeftExpression(new LongValue(1));
        deny.setRightExpression(new LongValue(0));
        return deny;
    }

    /**
     * 构造等值条件：{@code column = value}。
     *
     * @param column 列名（代码内常量，非用户输入）
     * @param value  值（已确保为 {@code long}）
     * @return {@link EqualsTo} 表达式
     */
    private static EqualsTo equals(String column, long value) {
        EqualsTo equals = new EqualsTo();
        equals.setLeftExpression(new Column(column));
        equals.setRightExpression(new LongValue(value));
        return equals;
    }

    /**
     * 构造 IN 条件：{@code column IN (v1, v2, ...)}。
     *
     * <p><b>JSqlParser 5.x 说明：</b>{@link ExpressionList} 在 5.x 中直接继承
     * {@code ArrayList<Expression>}，元素通过 {@code add()} 加入即可。</p>
     *
     * <p><b>JSqlParser 5.x 关键点：</b>IN 右侧必须使用
     * {@link ParenthesedExpressionList}，而不是普通的 {@link ExpressionList}。
     * 在 5.x 中两者已拆分：</p>
     * <ul>
     *   <li>{@code ExpressionList} 仅按逗号拼接元素，渲染为 {@code a, b, c}；</li>
     *   <li>{@code ParenthesedExpressionList} 额外包裹括号，渲染为 {@code (a, b, c)}。</li>
     * </ul>
     * 如果误用 {@code ExpressionList}，生成的 SQL 会变成
     * {@code class_id IN 1, 2, 3}（非法语法），而不是
     * {@code class_id IN (1, 2, 3)}。
     *
     * @param column 列名（代码内常量）
     * @param values 值集合（非空，由调用方保证）
     * @return {@link InExpression} 表达式
     */
    private static InExpression in(String column, Set<Long> values) {
        ParenthesedExpressionList<Expression> valueList = new ParenthesedExpressionList<>();
        for (Long value : values) {
            valueList.add(new LongValue(value));
        }
        InExpression in = new InExpression();
        in.setLeftExpression(new Column(column));
        in.setRightExpression(valueList);
        return in;
    }

}