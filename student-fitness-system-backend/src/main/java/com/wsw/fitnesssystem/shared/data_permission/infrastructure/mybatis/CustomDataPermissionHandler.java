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
 *   <li>表名识别通过 {@link #extractPrimaryTable(String)} 完成：从 mappedStatementId 中解析 Mapper 简单类名，
 *       再基于 {@link #MAPPER_TO_TABLE} 白名单映射到数据库表名。</li>
 *   <li>未登记或无法识别的 Mapper 会跳过数据权限处理，避免误伤无关查询。</li>
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
        log.debug("[DP-Handler] ctx: {}", ctx);

        String tableName = extractPrimaryTable(mappedStatementId);
        if (tableName == null) {
            log.debug("Cannot extract table name from [{}], skip data permission", mappedStatementId);
            return where;
        }
        log.debug("[DP-Handler] tableName: {}", tableName);

        Expression build = sqlBuilder.build(where, ctx, tableName);
        log.debug("[DP-Handler] build: {}", build);
        return build;
    }

    /**
     * Mapper 接口简单类名与数据库表名的白名单映射。
     * <p>只有登记在此处的 Mapper 才会参与数据权限过滤；未登记的 Mapper 会直接跳过。</p>
     * <p>新增需要数据权限控制的 Mapper 时，需要同步维护此映射。</p>
     */
    private static final Map<String, String> MAPPER_TO_TABLE = Map.of(
        "StudentProfileMapper", "student_profile",
        "UserProfileMapper", "user_profile",
        "TeacherProfileMapper", "teacher_profile",
        "SysUserMapper", "sys_user"
    );

    /**
     * 根据 MyBatis 的 mappedStatementId 推断当前 SQL 对应的主表名。
     *
     * <p>mappedStatementId 的格式通常为：</p>
     * <pre>
     * {Mapper 接口全限定名}.{方法名}
     * 例如：com.wsw.fitnesssystem.user.infrastructure.persistence.mapper.StudentProfileMapper.selectPage
     * </pre>
     *
     * <p>因此处理步骤为：</p>
     * <ol>
     *   <li>去掉最后一个 {@code .} 之后的方法名，得到 Mapper 接口全限定名；</li>
     *   <li>取 Mapper 接口全限定名的最后一段，得到 Mapper 接口简单类名；</li>
     *   <li>通过 {@link #MAPPER_TO_TABLE} 将 Mapper 简单类名映射为数据库表名。</li>
     * </ol>
     *
     * <p><b>返回规则：</b></p>
     * <ul>
     *   <li>mappedStatementId 为空或格式不符合约定时，返回 {@code null}；</li>
     *   <li>Mapper 简单类名未在 {@link #MAPPER_TO_TABLE} 中登记时，返回 {@code null}；</li>
     *   <li>命中映射时，返回对应的数据库表名。</li>
     * </ul>
     *
     * <p><b>设计说明：</b></p>
     * <ul>
     *   <li>不在本方法中解析 SQL，避免处理 JOIN、别名、子查询等复杂表名识别问题。</li>
     *   <li>采用显式白名单，只有确实需要数据权限控制的 Mapper 才登记，未登记则跳过，避免误伤无关查询。</li>
     *   <li>当前约定一个 Mapper 对应一张主表；若后续出现多表 JOIN、动态表名等场景，需要扩展映射策略或单独处理。</li>
     *   <li>新增需要数据权限的 Mapper 时，必须同步维护 {@link #MAPPER_TO_TABLE}。</li>
     * </ul>
     *
     * @param mappedStatementId MyBatis 执行的 statement id，格式为 Mapper 接口全限定名.方法名
     * @return 数据库表名；无法识别或未登记时返回 {@code null}，由调用方跳过数据权限
     */
    private String extractPrimaryTable(String mappedStatementId) {
        /* 从 mapper 接口名推断表名（约定：mapper 接口类名以 Mapper 结尾，
        * 如 StudentProfileMapper → student_profile）；
        * mappedStatementId 格式如：com.wsw...StudentProfileMapper.selectPage。 */

        /* 1. 防御性判空：mappedStatementId 为 null 时直接返回 null，调用方会跳过数据权限 */
        if (mappedStatementId == null) return null;

        /* 2. 定位最后一个 '.'：它分隔了 "Mapper 接口全限定名" 与 "方法名"。
        * 例如 com.wsw...StudentProfileMapper.selectPage ↑ 最后一个 '.' 之前是 Mapper 全限定名，
        * 之后是方法名 selectPage */
        int lastDot = mappedStatementId.lastIndexOf('.');
        // 不含 '.' 说明格式不符合约定，返回 null
        if (lastDot < 0) return null;

        /* 3. 截取最后一个 '.' 之前的部分，得到 Mapper 接口的全限定名；
        * 例如：com.wsw...StudentProfileMapper */
        String mapperSimpleName = mappedStatementId.substring(0, lastDot);

        /* 4. 再找一次 '.'，定位 Mapper 全限定名中的最后一个包分隔符，
        * 用它把 "包路径" 与 "Mapper 简单类名" 分开 */
        int prevDot = mapperSimpleName.lastIndexOf('.');

        /* 5. 截取最后一个 '.' 之后的部分，得到 Mapper 接口的简单类名；例如：StudentProfileMapper */
        mapperSimpleName = mapperSimpleName.substring(prevDot + 1);

        /* 6. 用 Mapper 简单类名去白名单 MAPPER_TO_TABLE 中查表名，
        * 命中 → 返回对应数据库表名；未登记 → 返回 null，调用方跳过数据权限。 */
        return MAPPER_TO_TABLE.get(mapperSimpleName);
    }

}
