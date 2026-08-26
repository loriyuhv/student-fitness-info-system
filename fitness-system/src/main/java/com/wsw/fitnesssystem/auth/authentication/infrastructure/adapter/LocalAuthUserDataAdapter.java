package com.wsw.fitnesssystem.auth.authentication.infrastructure.adapter;

import com.wsw.fitnesssystem.auth.authentication.application.dto.port.AuthUserData;
import com.wsw.fitnesssystem.auth.authentication.application.port.AuthUserDataProvider;
import com.wsw.fitnesssystem.user.application.UserAuthQueryService;
import com.wsw.fitnesssystem.user.application.dto.port.UserAuthData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/26 14:45
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class LocalAuthUserDataAdapter implements AuthUserDataProvider {

    private final UserAuthQueryService userAuthQueryService;

    @Override
    public AuthUserData getAuthDataByUsername(String username) {
        UserAuthData userAuthData = userAuthQueryService.getAuthUserData(username);

        if (userAuthData == null) {
            return null;
        }

        return AuthUserData.builder()
            .userId(userAuthData.getUserId())
            .campusId(userAuthData.getCampusId())
            .username(userAuthData.getUsername())
            .password(userAuthData.getPassword())
            .userType(userAuthData.getUserType())
            .status(userAuthData.getStatus())
            .build();
    }

}
