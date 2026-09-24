package com.wsw.fitnesssystem.iam.authentication.interfaces.web.dto.assembler;

import com.wsw.fitnesssystem.iam.authentication.application.dto.command.LoginCommand;
import com.wsw.fitnesssystem.iam.authentication.application.dto.result.LoginResult;
import com.wsw.fitnesssystem.iam.authentication.interfaces.web.dto.request.LoginRequest;
import com.wsw.fitnesssystem.iam.authentication.interfaces.web.dto.response.LoginResponse;
import com.wsw.fitnesssystem.iam.authentication.interfaces.web.support.ClientInfo;
import com.wsw.fitnesssystem.iam.authentication.interfaces.web.support.ClientInfoResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/21 12:04
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class LoginWebAssembler {

    private final ClientInfoResolver clientInfoResolver;

    public LoginCommand toCommand(
        LoginRequest loginRequest, HttpServletRequest httpServletRequest
    ) {
        ClientInfo clientInfo = clientInfoResolver.resolve(httpServletRequest);
        return new LoginCommand(
            loginRequest.username(),
            loginRequest.password(),
            clientInfo.deviceId(),
            clientInfo.deviceType(),
            clientInfo.clientIp(),
            clientInfo.userAgent()
        );
    }

    public LoginResponse toResponse(LoginResult result) {
        return new LoginResponse(
            result.accessToken(),
            result.refreshToken(),
            result.expiresIn()
        );
    }

}
