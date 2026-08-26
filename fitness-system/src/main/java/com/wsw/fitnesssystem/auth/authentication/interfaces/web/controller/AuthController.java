package com.wsw.fitnesssystem.auth.authentication.interfaces.web.controller;

import com.wsw.fitnesssystem.auth.authentication.application.AuthApplicationService;
import com.wsw.fitnesssystem.auth.authentication.application.dto.command.LoginCommand;
import com.wsw.fitnesssystem.auth.authentication.application.dto.command.RefreshCommand;
import com.wsw.fitnesssystem.auth.authentication.application.dto.result.LoginResult;
import com.wsw.fitnesssystem.auth.authentication.application.dto.result.RefreshResult;
import com.wsw.fitnesssystem.auth.authentication.application.vo.UserInfoVO;
import com.wsw.fitnesssystem.auth.authentication.infrastructure.security.model.JwtUserPrincipal;
import com.wsw.fitnesssystem.auth.authentication.interfaces.web.dto.response.LoginResponse;
import com.wsw.fitnesssystem.auth.authentication.interfaces.web.dto.request.RefreshRequest;
import com.wsw.fitnesssystem.auth.authentication.interfaces.web.dto.response.RefreshResponse;
import com.wsw.fitnesssystem.auth.shared.utils.WebUtils;
import com.wsw.fitnesssystem.shared.context.RequestContextHolder;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import com.wsw.fitnesssystem.shared.response.ApiResult;
import com.wsw.fitnesssystem.auth.authentication.interfaces.web.dto.request.LoginRequest;
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
        @RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest) {

        // 1. Web层：提取Web特有数据（IP、User-Agent），构建Application层的输入Command
        LoginCommand command = LoginCommand.builder()
            .username(request.getUsername())
            .password(request.getPassword())
            .deviceType(request.getDeviceType())
            .deviceId(httpRequest.getHeader("X-Device-Id"))
            .ip(WebUtils.getClientIp(httpRequest))
            .userAgent(httpRequest.getHeader("User-Agent"))
            .build();

        // 2. 调用Application层（核心业务逻辑），得到纯业务输出
        LoginResult login = authApplicationService.login(command);

        // 3. 防腐层转换：将业务输出（LoginResult）转换为协议输出（LoginResponse）
        LoginResponse response = LoginResponse.builder()
            .accessToken(login.getAccessToken())
            .refreshToken(login.getRefreshToken())
            .expiresIn(login.getExpiresIn())
            .build();

        return ApiResult.success(response);

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
    public ApiResult<RefreshResponse> refresh(@RequestBody @Valid RefreshRequest request) {

        RefreshCommand command = RefreshCommand.builder()
            .refreshToken(request.getRefreshToken())
            .build();

        RefreshResult result = authApplicationService.refreshAccessToken(command);

        RefreshResponse response = RefreshResponse.builder()
            .accessToken(result.getAccessToken())
            .refreshToken(result.getRefreshToken())
            .expiresIn(result.getExpiresIn())
            .build();

        return ApiResult.success(response);

    }

    @GetMapping("/user-info")
    public ApiResult<UserInfoVO> userInfo() {
        Operator operator = RequestContextHolder.getRequiredOperator();
        return ApiResult.success(authApplicationService.getUserInfo(operator));
    }

}
