package com.wsw.fitnesssystem.auth.authentication.infrastructure.adapter;

import com.wsw.fitnesssystem.auth.authentication.application.dto.port.AuthUserCredential;
import com.wsw.fitnesssystem.auth.authentication.application.port.AuthUserDataProvider;
import com.wsw.fitnesssystem.user.application.service.UserAuthQueryService;
import com.wsw.fitnesssystem.user.application.dto.port.UserAuthData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 本地适配器（单体/模块化阶段使用）
 * <p>通过直接调用 User 模块的 {@link UserAuthQueryService} 获取数据，
 *  * 将 User 模块的 {@code UserAuthData} 转换为 Auth 模块的 {@code AuthUserCredential}。
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 14:45
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalAuthUserDataAdapter implements AuthUserDataProvider {

    private final UserAuthQueryService userAuthQueryService;

    @Override
    public AuthUserCredential getAuthDataByUsername(String username) {

        log.debug("Fetching auth data from local user module for username: {}", username);

        UserAuthData userAuthData = userAuthQueryService.getAuthUserData(username);

        if (userAuthData == null) {
            log.warn("User not found in local user module: {}", username);
            return null;
        }

        return AuthUserCredential.builder()
            .userId(userAuthData.getUserId())
            .campusId(userAuthData.getCampusId())
            .username(userAuthData.getUsername())
            .password(userAuthData.getPassword())
            .userType(userAuthData.getUserType())
            .status(userAuthData.getStatus())
            .build();
    }

    @Override
    public AuthUserCredential getAuthDataByCampusIdAndUserId(long campusId, long userId) {
        UserAuthData userAuthData = userAuthQueryService.getAuthUserData(campusId, userId);

        if (userAuthData == null) {
            return null;
        }

        return AuthUserCredential.builder()
            .userId(userAuthData.getUserId())
            .campusId(userAuthData.getCampusId())
            .username(userAuthData.getUsername())
            .password(userAuthData.getPassword())
            .userType(userAuthData.getUserType())
            .status(userAuthData.getStatus())
            .build();
    }

}
