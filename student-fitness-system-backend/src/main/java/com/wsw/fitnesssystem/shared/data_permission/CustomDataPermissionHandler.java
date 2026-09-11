package com.wsw.fitnesssystem.shared.data_permission;

import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import org.springframework.stereotype.Component;

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
public class CustomDataPermissionHandler implements DataPermissionHandler {

    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        DataPermissionContext ctx = DataPermissionContextHolder.getContext();
        if (ctx == null || ctx.isAllVisible()) {
            // 无上下文或全部可见，原样返回
            return where;
        }

        // 由子类或工具方法根据 ctx 生成过滤条件
        // 实际实现需要用 JSqlParser 构造表达式，这里给出架构骨架
        return DataPermissionSqlBuilder.build(where, ctx);
    }

}
