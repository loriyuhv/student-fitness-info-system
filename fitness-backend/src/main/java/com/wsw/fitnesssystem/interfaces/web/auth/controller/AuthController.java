package com.wsw.fitnesssystem.interfaces.web.auth.controller;

import com.wsw.fitnesssystem.application.auth.AuthApplicationService;
import com.wsw.fitnesssystem.application.auth.dto.LoginResult;
import com.wsw.fitnesssystem.interfaces.response.ApiResult;
import com.wsw.fitnesssystem.interfaces.web.auth.dto.LoginRequest;
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

    /**
     * 登录
     */
    @PostMapping("/login")
    public ApiResult<LoginResult> login(
        @RequestBody @Valid LoginRequest request,
        HttpServletRequest httpRequest
    ) {
        String username = request.getUsername();
        String password = request.getPassword();
        String deviceType = request.getDeviceType();
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");


        LoginResult result =
            authApplicationService.login(
                username,
                password,
                ip,
                deviceType,
                userAgent
            );

        return ApiResult.success(result);
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
