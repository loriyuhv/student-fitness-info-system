package com.wsw.fitnesssystem.auth.interfaces.web;

import com.wsw.fitnesssystem.auth.application.authentication.AuthApplicationService;
import com.wsw.fitnesssystem.auth.application.authentication.command.LoginCommand;
import com.wsw.fitnesssystem.auth.application.authentication.dto.LoginResponse;
import com.wsw.fitnesssystem.auth.application.authentication.dto.RefreshTokenResponse;
import com.wsw.fitnesssystem.auth.application.authentication.vo.UserInfoVO;
import com.wsw.fitnesssystem.auth.infrastructure.security.model.JwtUserPrincipal;
import com.wsw.fitnesssystem.auth.interfaces.web.dto.RefreshRequest;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import com.wsw.fitnesssystem.shared.response.ApiResult;
import com.wsw.fitnesssystem.auth.interfaces.web.dto.LoginRequest;
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
            .ip(getClientIp(httpRequest))
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
            Long campusId = principal.campusId();
            Long userId = principal.userId();

            Operator operator = new Operator(campusId, userId, null, null);
            // 1. 调用 Application Service 协调登出
            authApplicationService.logout(operator, accessTokenId);

            // 3. 返回成功
            return ApiResult.success(ResultCode.LOGOUT_SUCCESS.getMessage(), null);
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
        return ApiResult.success(authApplicationService.getUserInfo());
    }

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
