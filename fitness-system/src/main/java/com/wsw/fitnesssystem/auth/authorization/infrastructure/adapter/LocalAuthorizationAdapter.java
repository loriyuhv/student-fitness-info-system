package com.wsw.fitnesssystem.auth.authorization.infrastructure.adapter;

import com.wsw.fitnesssystem.auth.authentication.application.dto.port.AuthAuthorization;
import com.wsw.fitnesssystem.auth.authentication.application.port.AuthorizationPort;
import com.wsw.fitnesssystem.auth.authorization.application.dto.query.AuthorizationQuery;
import com.wsw.fitnesssystem.auth.authorization.application.dto.result.UserAuthorization;
import com.wsw.fitnesssystem.auth.authorization.application.service.AuthorizationQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/26 18:46
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalAuthorizationAdapter implements AuthorizationPort {

    private final AuthorizationQueryService authorizationQueryService;

    @Override
    public AuthAuthorization getAuthorization(long userId, long campusId) {
        log.debug("🔐 [LocalAuth] Fetching authorization for user: {}", userId);

        AuthorizationQuery query = AuthorizationQuery.builder()
            .userId(userId)
            .campusId(campusId)
            .build();

       UserAuthorization authorize = authorizationQueryService.authorize(query);

        return AuthAuthorization.builder()
            .userId(authorize.getUserId())
            .campusId(authorize.getCampusId())
            .roles(authorize.getRoles())
            .permissions(authorize.getPermissions())
            .build();
    }

    @Override
    public void removeAuthorization(long userId, long campusId) {
        log.debug("🔐 [LocalAuth] Removing authorization for user: {}, campus: {}", userId, campusId);
        authorizationQueryService.removeAuthorization(userId, campusId);
    }
}
