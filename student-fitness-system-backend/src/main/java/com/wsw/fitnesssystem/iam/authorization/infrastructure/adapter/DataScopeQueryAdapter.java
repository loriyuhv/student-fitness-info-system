package com.wsw.fitnesssystem.iam.authorization.infrastructure.adapter;

import com.wsw.fitnesssystem.shared.data_permission.DataScope;
import com.wsw.fitnesssystem.shared.data_permission.DataScopeQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 数据权限规则查询适配器。
 * <p>实现 shared 定义的 {@link DataScopeQueryPort}，返回用户的最大 data_scope。</p>
 *
 * <p><b>数据来源：</b>{@code sys_user_role} JOIN {@code sys_role}。</p>
 * <p><b>建议：</b>结果可缓存在 Redis（{@code iam:perm:user:{campusId}:{userId}}），
 * 角色变更时失效。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/11 12:33
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataScopeQueryAdapter implements DataScopeQueryPort {

    @Override
    public DataScope queryMaxDataScope(Long userId, Long campusId) {
        return null;
    }

}
