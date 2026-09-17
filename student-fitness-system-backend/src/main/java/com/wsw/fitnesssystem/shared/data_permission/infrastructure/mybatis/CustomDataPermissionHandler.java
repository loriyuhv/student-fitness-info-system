package com.wsw.fitnesssystem.shared.data_permission.infrastructure.mybatis;

import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.wsw.fitnesssystem.shared.data_permission.context.DataPermissionContextHolder;
import com.wsw.fitnesssystem.shared.data_permission.application.builder.DataPermissionSqlBuilder;
import com.wsw.fitnesssystem.shared.data_permission.domain.DataPermissionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 数据权限处理器（MyBatis-Plus 拦截器适配层）。
 *
 * <p><b>职责：</b>从 {@link DataPermissionContextHolder} 读取规则，
 * 通过 {@link DataPermissionSqlBuilder} 生成过滤条件，追加到原 SQL。</p>
 *
 * <p><b>表名识别策略：</b>从 {@code mappedStatementId} 解析 Mapper 接口简单类名，
 * 再基于 {@link #MAPPER_TO_TABLE} 白名单映射到数据库表名。</p>
 *
 * <p><b>Fail-safe 策略：</b>无法识别表名时不追加条件（视为"非业务表"），
 * 但若表名可识别却在 {@code DataPermissionRuleRegistry} 中未注册，
 * 则由 {@link DataPermissionSqlBuilder} 拒绝。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/10 12:51
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomDataPermissionHandler implements DataPermissionHandler {

    private final DataPermissionSqlBuilder sqlBuilder;

    /**
     * Mapper 接口简单类名 → 数据库表名 的白名单映射。
     *
     * <p><b>维护规则：</b></p>
     * <ul>
     *   <li>登记此处的 Mapper → 参与数据权限过滤；</li>
     *   <li>登记但表在 Registry 中未注册 → 由 Builder 拒绝（fail-safe）；</li>
     *   <li>不登记此处的 Mapper → 视为"非业务表查询"，直接跳过。</li>
     * </ul>
     */
    private static final Map<String, String> MAPPER_TO_TABLE = Map.ofEntries(
        /* ============ user 模块 ============ */
        Map.entry("StudentProfileMapper", "student_profile"),
        Map.entry("UserProfileMapper",    "user_profile"),
        Map.entry("TeacherProfileMapper", "teacher_profile"),
        Map.entry("SysUserMapper",        "sys_user"),

        /* ============ fitness 模块 ============ */
        Map.entry("FitnessRecordMapper",  "fitness_record"),

        /* ============ 公共表（显式登记以便走豁免分支） ============ */
        Map.entry("ClassInfoMapper",      "class_info"),
        Map.entry("TeacherClassMapper",   "teacher_class")
    );

    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        DataPermissionContext ctx = DataPermissionContextHolder.getContext();
        if (ctx == null || ctx.isAllVisible()) {
            return where;
        }
        log.debug("[DP-Handler] ctx: {}", ctx);

        String tableName = extractPrimaryTable(mappedStatementId);
        if (tableName == null) {
            log.debug("[DP-Handler] Cannot extract table from [{}], skip",
                mappedStatementId);
            return where;
        }
        log.debug("[DP-Handler] resolved table: {}", tableName);

        Expression result = sqlBuilder.build(where, ctx, tableName);
        log.debug("[DP-Handler] final expression: {}", result);
        return result;
    }

    /**
     * 根据 MyBatis 的 {@code mappedStatementId} 推断当前 SQL 对应的主表名。
     *
     * <p>处理步骤：</p>
     * <ol>
     *   <li>去掉最后一个 {@code .} 之后的方法名，得到 Mapper 全限定名；</li>
     *   <li>取最后一段，得到 Mapper 简单类名；</li>
     *   <li>通过 {@link #MAPPER_TO_TABLE} 映射为数据库表名。</li>
     * </ol>
     *
     * @param mappedStatementId 格式：{Mapper 全限定名}.{方法名}
     * @return 数据库表名；无法识别或未登记时返回 {@code null}
     */
    private String extractPrimaryTable(String mappedStatementId) {
        if (mappedStatementId == null) return null;

        int lastDot = mappedStatementId.lastIndexOf('.');
        if (lastDot < 0) return null;

        String mapperFullName = mappedStatementId.substring(0, lastDot);

        int prevDot = mapperFullName.lastIndexOf('.');
        String mapperSimpleName = mapperFullName.substring(prevDot + 1);

        return MAPPER_TO_TABLE.get(mapperSimpleName);
    }

}
