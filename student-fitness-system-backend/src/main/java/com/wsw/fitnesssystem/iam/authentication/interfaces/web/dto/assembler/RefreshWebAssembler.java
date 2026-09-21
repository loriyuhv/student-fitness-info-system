package com.wsw.fitnesssystem.iam.authentication.interfaces.web.dto.assembler;

import com.wsw.fitnesssystem.iam.authentication.application.dto.command.RefreshCommand;
import com.wsw.fitnesssystem.iam.authentication.application.dto.result.RefreshResult;
import com.wsw.fitnesssystem.iam.authentication.interfaces.web.dto.request.RefreshRequest;
import com.wsw.fitnesssystem.iam.authentication.interfaces.web.dto.response.RefreshResponse;
import com.wsw.fitnesssystem.iam.authentication.interfaces.web.support.ClientInfo;
import com.wsw.fitnesssystem.iam.authentication.interfaces.web.support.ClientInfoResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/21 13:16
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class RefreshWebAssembler {

    private final ClientInfoResolver clientInfoResolver;

    public RefreshCommand toCommand(RefreshRequest request, HttpServletRequest httpServletRequest) {
        ClientInfo clientInfo = clientInfoResolver.resolve(httpServletRequest);
        return new RefreshCommand(
            request.refreshToken(),
            request.deviceType(),
            clientInfo.userAgent(),
            clientInfo.clientIp()
        );
    }

    public RefreshResponse toResponse(RefreshResult result) {
        return new RefreshResponse(
            result.accessToken(),
            result.refreshToken(),
            result.expiresIn()
        );
    }

}
