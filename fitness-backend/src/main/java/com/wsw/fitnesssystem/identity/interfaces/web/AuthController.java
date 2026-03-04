package com.wsw.fitnesssystem.identity.interfaces.web;

import com.wsw.fitnesssystem.identity.application.authentication.AuthApplicationService;
import com.wsw.fitnesssystem.auth.application.command.LoginCommand;
import com.wsw.fitnesssystem.auth.application.dto.LoginResult;
import com.wsw.fitnesssystem.shared.response.ApiResult;
import com.wsw.fitnesssystem.identity.interfaces.web.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 *
 * @author loriyuhv
 * @version 1.0 2026/1/11 15:45
 * @since 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthApplicationService authApplicationService;

    @PostMapping("/login")
    public ApiResult<LoginResult> login(
        @RequestBody @Valid LoginRequest request,
        HttpServletRequest httpRequest
    ) {
        LoginCommand command = LoginCommand.builder()
            .username(request.getUsername())
            .password(request.getPassword())
            .deviceType(request.getDeviceType())
            .ip(getClientIp(httpRequest))
            .userAgent(httpRequest.getHeader("User-Agent"))
            .build();

        return ApiResult.success(authApplicationService.login(command));
    }

    /**
     * 退出当前登录
     */
    @PostMapping("/logout")
    public ApiResult<Void> logout(HttpServletRequest request) {
        // Long userId = JwtContextHolder.getUserId();
        // String tokenId = JwtContextHolder.getTokenId();
        // authService.logout(userId, tokenId);
        // return ApiResult.success();
        return ApiResult.success("用户退出成功！！！", null);
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
