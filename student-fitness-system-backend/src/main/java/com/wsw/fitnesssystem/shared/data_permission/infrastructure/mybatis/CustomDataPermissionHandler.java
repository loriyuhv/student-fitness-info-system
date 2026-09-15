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
 * 数据权限处理器。
 * <p>从 {@link DataPermissionContextHolder} 读取规则，动态向 SQL 追加 WHERE 条件。</p>
 *
 * <p><b>架构注意：</b></p>
 * <ul>
 *   <li>本类不查询数据库，只消费 Context 中的规则（Context 在请求入口已组装）。</li>
 *   <li>Context 为空（未登录、无需过滤）时，直接返回原条件，不追加任何过滤。</li>
 * </ul>
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

    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        DataPermissionContext ctx = DataPermissionContextHolder.getContext();
        if (ctx == null || ctx.isAllVisible()) {
            return where;
        }
        log.debug("data permission handler ==> ctx: {}", ctx);

        String tableName = extractPrimaryTable(mappedStatementId);
        if (tableName == null) {
            log.debug("Cannot extract table name from [{}], skip data permission", mappedStatementId);
            return where;
        }
        log.debug("data permission handler ==> tableName: {}", tableName);

        // return sqlBuilder.build(where, ctx, tableName);

        Expression build = sqlBuilder.build(where, ctx, tableName);
        log.debug("data permission handler ==> build: {}", build);
        return build;
    }

    private static final Map<String, String> MAPPER_TO_TABLE = Map.of(
        "StudentProfileMapper", "student_profile",
        "UserProfileMapper", "user_profile",
        "TeacherProfileMapper", "teacher_profile",
        "SysUserMapper", "sys_user"
    );

    /**
     * 从 mappedStatementId 中无法直接拿表名，改为解析 SQL。
     * <p>MyBatis-Plus 的 DataPermissionInterceptor 会先解析 SQL 再回调本方法，
     * 但从接口签名拿不到 SQL，这里做一个保守处理——不解析，交由 MP 内部机制。</p>
     *
     * <p><b>说明：</b>MyBatis-Plus 3.5.5+ 的 {@code DataPermissionHandler} 接口
     * 提供了 {@code getSqlSegment(Expression, String)}，其中 mappedStatementId 无法直接
     * 定位表名。当前实现返回 {@code null} 表示"跳过表名解析"，
     * 由 {@link DataPermissionSqlBuilder} 的 {@code targetTable} 参数接收
     * 由上层拦截器通过其他方式注入（见下方兼容方案）。</p>
     *
     * <p><b>兼容方案：</b>由于无法从接口签名拿到 SQL，采用"表名优先，字段名兜底"策略：
     * 若无法定位表名，直接使用当前请求范围内的所有已注册表的字段映射。
     * 该策略在多表 JOIN 场景下可能过度过滤，但对于单表查询是安全的。</p>
     */
    private String extractPrimaryTable(String mappedStatementId) {
        // 从 mapper 接口名推断表名（约定：mapper 接口类名以 Mapper 结尾，如 StudentProfileMapper → student_profile）
        // 简化实现：返回 null，由 builder 侧基于字段名猜测
        // mappedStatementId 格式如：com.wsw...StudentProfileMapper.selectPage
        if (mappedStatementId == null) return null;
        int lastDot = mappedStatementId.lastIndexOf('.');
        if (lastDot < 0) return null;
        String mapperSimpleName = mappedStatementId.substring(0, lastDot);
        int prevDot = mapperSimpleName.lastIndexOf('.');
        mapperSimpleName = mapperSimpleName.substring(prevDot + 1);

        return MAPPER_TO_TABLE.get(mapperSimpleName);
    }
}
