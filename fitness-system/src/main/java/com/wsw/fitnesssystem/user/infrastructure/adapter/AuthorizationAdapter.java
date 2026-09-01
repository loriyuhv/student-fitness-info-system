package com.wsw.fitnesssystem.user.infrastructure.adapter;

import com.wsw.fitnesssystem.auth.authorization.application.dto.query.AuthorizationQuery;
import com.wsw.fitnesssystem.auth.authorization.application.dto.result.UserAuthorization;
import com.wsw.fitnesssystem.auth.authorization.application.service.AuthorizationQueryService;
import com.wsw.fitnesssystem.user.application.dto.port.UserAuthorizationInfo;
import com.wsw.fitnesssystem.user.application.port.AuthorizationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 00:26
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class AuthorizationAdapter implements AuthorizationPort {

    private final AuthorizationQueryService authorizationQueryService;

    @Override
    public UserAuthorizationInfo getAuthorizations(Long userId, Long campusId) {
        AuthorizationQuery query = AuthorizationQuery.builder().userId(userId).campusId(campusId).build();
        UserAuthorization authorize = authorizationQueryService.authorize(query);
        return UserAuthorizationInfo.builder()
            .permissions(authorize.getPermissions())
            .roles(authorize.getRoles())
            .build();
    }
}
