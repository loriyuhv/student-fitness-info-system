package com.wsw.fitnesssystem.iam.authorization.infrastructure.adapter;

import com.wsw.fitnesssystem.iam.authorization.application.dto.query.AuthorizationQuery;
import com.wsw.fitnesssystem.iam.authorization.application.dto.result.UserAuthorization;
import com.wsw.fitnesssystem.iam.authorization.application.service.AuthorizationQueryService;
import com.wsw.fitnesssystem.user.application.dto.result.UserAuthorizationResult;
import com.wsw.fitnesssystem.user.application.port.output.UserAuthorizationQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户授权查询本地适配器。
 *
 * <p><b>职责：</b>实现 user 模块定义的 {@link UserAuthorizationQueryPort}，
 * 将 authorization 内部模型 {@link UserAuthorization} 转换为
 * user 模块所需的 {@link UserAuthorizationResult}。</p>
 *
 * <p><b>依赖方向：</b>本类依赖 user 模块的 Port 和 DTO（契约），
 * user 不反向依赖 authorization。</p>
 *
 * <p><b>缓存策略：</b>通过 {@link AuthorizationQueryService} 间接使用
 * Redis 缓存，Adapter 本身不感知缓存细节。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 15:37
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthorizationQueryLocalAdapter implements UserAuthorizationQueryPort {

    private final AuthorizationQueryService authorizationQueryService;

    @Override
    public UserAuthorizationResult findByUserIdAndCampusId(Long userId, Long campusId) {
        if (userId == null || campusId == null) {
            return UserAuthorizationResult.empty();
        }

        AuthorizationQuery query = AuthorizationQuery.builder()
            .userId(userId)
            .campusId(campusId)
            .build();

        UserAuthorization userAuth = authorizationQueryService.authorize(query);

        return UserAuthorizationResult.builder()
            .roles(userAuth.getRoles())
            .permissions(userAuth.getPermissions())
            .build();
    }

}
