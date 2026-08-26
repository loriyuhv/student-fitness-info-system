package com.wsw.fitnesssystem.auth.authentication.interfaces.web;

import com.wsw.fitnesssystem.auth.authentication.application.AuthApplicationService;
import com.wsw.fitnesssystem.auth.authentication.application.command.LoginCommand;
import com.wsw.fitnesssystem.auth.authentication.application.dto.LoginResponse;
import com.wsw.fitnesssystem.auth.authentication.application.dto.RefreshTokenResponse;
import com.wsw.fitnesssystem.auth.authentication.application.vo.UserInfoVO;
import com.wsw.fitnesssystem.auth.authentication.infrastructure.security.model.JwtUserPrincipal;
import com.wsw.fitnesssystem.auth.authentication.interfaces.web.dto.RefreshRequest;
import com.wsw.fitnesssystem.auth.shared.utils.WebUtils;
import com.wsw.fitnesssystem.shared.context.RequestContextHolder;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import com.wsw.fitnesssystem.shared.response.ApiResult;
import com.wsw.fitnesssystem.auth.authentication.interfaces.web.dto.LoginRequest;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author loriyuhv
 * @version 1.0 2026/1/11 15:45
 * @since 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthApplicationService authApplicationService;

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(
        @RequestBody @Valid LoginRequest request,
        HttpServletRequest httpRequest
    ) {
        LoginCommand command = LoginCommand.builder()
            .username(request.getUsername())
            .password(request.getPassword())
            .deviceType(request.getDeviceType())
            .deviceId(httpRequest.getHeader("X-Device-Id"))
            .ip(WebUtils.getClientIp(httpRequest))
            .userAgent(httpRequest.getHeader("User-Agent"))
            .build();

        return ApiResult.success(authApplicationService.login(command));
    }

    /**
     * 退出当前登录
     */
    @PostMapping("/logout")
    public ApiResult<Void> logout() {
        try {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
            String accessTokenId = principal.accessTokenId();

            Operator operator = RequestContextHolder.getRequiredOperator();

            // 1. 调用 Application Service 协调登出
            authApplicationService.logout(operator, accessTokenId);

            // 3. 返回成功
            return ApiResult.success(ResultCode.LOGOUT_SUCCESS);

        } catch (Exception e) {
            log.error(ResultCode.LOGOUT_FAILED.getMessage(), e);
            return ApiResult.error(ResultCode.LOGOUT_FAILED);
        }
    }

    /**
     *  刷新Token
     *  */
    @PostMapping("/refresh")
    public ApiResult<RefreshTokenResponse> refresh(
            @RequestBody @Valid RefreshRequest request
    ) {
        return ApiResult.success(authApplicationService.refreshAccessToken(request.getRefreshToken()));
    }

    @GetMapping("/user-info")
    public ApiResult<UserInfoVO> userInfo() {
        Operator operator = RequestContextHolder.getRequiredOperator();
        return ApiResult.success(authApplicationService.getUserInfo(operator));
    }

}
