package com.wsw.fitnesssystem.shared.data_permission.application.builder;

import com.wsw.fitnesssystem.shared.data_permission.application.registry.DataPermissionRuleRegistry;
import com.wsw.fitnesssystem.shared.data_permission.domain.DataPermissionContext;
import com.wsw.fitnesssystem.shared.data_permission.domain.DataPermissionRule;
import com.wsw.fitnesssystem.shared.data_permission.domain.DataScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 数据权限 SQL 条件构造器。
 *
 * <p><b>职责：</b>根据 {@link DataPermissionContext} 与目标表的 {@link DataPermissionRule}，
 * 生成 JSqlParser 的 {@link Expression}，由 {@code CustomDataPermissionHandler} 追加到原 SQL。</p>
 *
 * <p><b>决策流程（按顺序）：</b></p>
 * <ol>
 *   <li>ctx 为空 / ALL 可见 → 原样返回，不加条件；</li>
 *   <li>表未注册 → <b>拒绝</b>（fail-safe）；</li>
 *   <li>表显式豁免 → 原样返回，不加条件；</li>
 *   <li>契约不支持当前 scope → <b>拒绝</b>（契约冲突）；</li>
 *   <li>生成对应 scope 的过滤条件并 AND 到原 WHERE。</li>
 * </ol>
 *
 * <p><b>契约一致性保证：</b>由于 {@link DataPermissionRule} 紧凑构造器已校验
 * "声明支持某 scope 则对应列必非 null"，本类不再需要 {@code if (column == null)} 判断。</p>
 *
 * <p><b>缺省策略：</b>所有无法满足的分支统一拒绝（{@code 1 = 0}），而非静默放行。
 * 拒绝是可用性问题，可观测、可修复；放行是数据越权，属于安全红线。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/10 12:53
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataPermissionSqlBuilder {

    private final DataPermissionRuleRegistry ruleRegistry;

    // ==================== 对外入口 ====================

    /**
     * 生成数据权限过滤表达式，与原 WHERE 条件 AND 组合。
     *
     * @param where       原 WHERE 条件，可能为 {@code null}
     * @param ctx         数据权限上下文，可能为 {@code null}
     * @param targetTable 目标表名（不带别名）
     * @return 组合后的表达式；无需过滤时原样返回 {@code where}
     */
    public Expression build(Expression where, DataPermissionContext ctx, String targetTable) {
        // 快速返回：ctx 为空 或 ALL 可见
        if (ctx == null || ctx.isAllVisible()) {
            return where;
        }

        DataPermissionRule rule = ruleRegistry.find(targetTable);

        // 分支 1：表未注册 → 拒绝（fail-safe）
        if (rule == null) {
            log.warn("[DP] Table [{}] not registered, deny (fail-safe)", targetTable);
            return and(where, buildDeny());
        }

        // 分支 2：显式豁免 → 放行
        if (rule.exempt()) {
            log.debug("[DP] Table [{}] is exempt from data permission", targetTable);
            return where;
        }

        // 分支 3：契约不支持当前 scope → 拒绝（契约冲突）
        DataScope scope = ctx.dataScope();
        if (!rule.supports(scope)) {
            log.warn("[DP] Table [{}] does not support scope {}, deny (contract violation)",
                targetTable, scope);
            return and(where, buildDeny());
        }

        // 分支 4：按 scope 生成条件
        Expression condition = switch (scope) {
            case SELF    -> buildSelf(rule, ctx);
            case CLASS   -> buildClasses(rule, ctx);
            case COLLEGE -> buildCampus(rule, ctx);
            case CUSTOM  -> buildDeny();
            case ALL     -> null;  // isAllVisible 已处理，此处兜底
        };

        return and(where, condition);
    }

    // ==================== 各维度条件构造 ====================
    // 说明：因契约已校验，rule.xxxColumn() 在此必非 null（由 DataPermissionRule 保证）

    private Expression buildSelf(DataPermissionRule rule, DataPermissionContext ctx) {
        if (ctx.currentUserId() == null) {
            log.warn("[DP] SELF scope but currentUserId is null, deny");
            return buildDeny();
        }
        return equals(rule.selfColumn(), ctx.currentUserId());
    }

    private Expression buildClasses(DataPermissionRule rule, DataPermissionContext ctx) {
        Set<Long> classIds = ctx.allowedClassIds();
        if (classIds == null || classIds.isEmpty()) {
            log.warn("[DP] CLASS scope but no allowed classes, deny");
            return buildDeny();
        }
        return in(rule.classColumn(), classIds);
    }

    private Expression buildCampus(DataPermissionRule rule, DataPermissionContext ctx) {
        if (ctx.currentCampusId() == null) {
            log.warn("[DP] COLLEGE scope but currentCampusId is null, deny");
            return buildDeny();
        }
        return equals(rule.campusColumn(), ctx.currentCampusId());
    }

    // ==================== 通用表达式构造 ====================

    /** 构造恒假条件 {@code 1 = 0}，用于"请求了权限但无法满足"的保守拒绝。 */
    private Expression buildDeny() {
        EqualsTo deny = new EqualsTo();
        deny.setLeftExpression(new LongValue(1));
        deny.setRightExpression(new LongValue(0));
        return deny;
    }

    /** 构造等值条件：{@code column = value}。 */
    private static EqualsTo equals(String column, long value) {
        EqualsTo equals = new EqualsTo();
        equals.setLeftExpression(new Column(column));
        equals.setRightExpression(new LongValue(value));
        return equals;
    }

    /**
     * 构造 IN 条件：{@code column IN (v1, v2, ...)}。
     *
     * <p><b>JSqlParser 5.x 关键点：</b>IN 右侧必须用 {@link ParenthesedExpressionList}，
     * 否则生成的 SQL 会缺少括号，成为非法语法。</p>
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

    /**
     * AND 组合工具：任一为 {@code null} 时返回另一个。
     * <p>用于统一"原 WHERE + 权限条件"的合并逻辑，避免重复三元表达式。</p>
     */
    private static Expression and(Expression a, Expression b) {
        if (a == null) return b;
        if (b == null) return a;
        return new AndExpression(a, b);
    }

}