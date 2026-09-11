package com.wsw.fitnesssystem.iam.authorization.infrastructure.adapter;

import com.wsw.fitnesssystem.iam.authorization.domain.port.AuthorizationRepository;
import com.wsw.fitnesssystem.shared.data_permission.DataScope;
import com.wsw.fitnesssystem.shared.data_permission.DataScopeQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 数据权限规则查询适配器（Infrastructure 层）。
 * <p>
 * 实现 shared 模块定义的 {@link DataScopeQueryPort}，返回用户的最大 data_scope。
 * </p>
 *
 * <p><b>数据来源：</b>{@code sys_user_role} JOIN {@code sys_role}，
 * 通过 Domain 层的 {@link AuthorizationRepository} 间接访问，
 * 不直接依赖 MyBatis Mapper。</p>
 *
 * <p><b>多角色处理：</b>取 data_scope 最小值（数字越小权限越大）。
 * 若用户无任何启用角色，返回 {@link DataScope#SELF}（最小权限兜底）。</p>
 *
 * <p><b>缓存建议：</b>可在适配器内部引入缓存（Redis），
 * 避免每次请求都查询数据库。缓存失效时机：用户角色变更、角色状态变更。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/11 12:33
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataScopeQueryAdapter implements DataScopeQueryPort {

    private final AuthorizationRepository authorizationRepository;

    @Override
    public DataScope queryMaxDataScope(Long userId, Long campusId) {
        // 参数防御
        if (userId == null || campusId == null) {
            log.debug("Skip data scope query for null userId or campusId");
            return DataScope.SELF;
        }

        Integer minDataScope = authorizationRepository.findMinDataScope(userId, campusId);

        // 无角色时，兜底为 SELF（仅本人），避免默认放开权限
        if (minDataScope == null) {
            log.debug("No role found for user {}, fallback to SELF", userId);
            return DataScope.SELF;
        }

        DataScope scope = DataScope.of(minDataScope);
        log.debug("User {} resolved data scope: {}", userId, scope);
        return scope;
    }

}
