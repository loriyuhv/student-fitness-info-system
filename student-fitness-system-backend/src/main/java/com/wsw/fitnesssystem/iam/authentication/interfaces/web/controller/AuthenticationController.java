package com.wsw.fitnesssystem.iam.authentication.interfaces.web.controller;

import com.wsw.fitnesssystem.iam.authentication.application.dto.command.LoginCommand;
import com.wsw.fitnesssystem.iam.authentication.application.dto.command.LogoutCommand;
import com.wsw.fitnesssystem.iam.authentication.application.dto.command.RefreshCommand;
import com.wsw.fitnesssystem.iam.authentication.application.dto.result.LoginResult;
import com.wsw.fitnesssystem.iam.authentication.application.dto.result.RefreshResult;
import com.wsw.fitnesssystem.iam.authentication.application.service.command.LoginCommandService;
import com.wsw.fitnesssystem.iam.authentication.application.service.command.LogoutCommandService;
import com.wsw.fitnesssystem.iam.authentication.application.service.command.TokenRefreshCommandService;
import com.wsw.fitnesssystem.iam.authentication.interfaces.web.dto.assembler.LoginWebAssembler;
import com.wsw.fitnesssystem.iam.authentication.interfaces.web.dto.assembler.LogoutWebAssembler;
import com.wsw.fitnesssystem.iam.authentication.interfaces.web.dto.assembler.RefreshWebAssembler;
import com.wsw.fitnesssystem.iam.authentication.interfaces.web.dto.response.LoginResponse;
import com.wsw.fitnesssystem.iam.authentication.interfaces.web.dto.request.RefreshRequest;
import com.wsw.fitnesssystem.iam.authentication.interfaces.web.dto.response.RefreshResponse;
import com.wsw.fitnesssystem.shared.interfaces.web.response.ApiResponse;
import com.wsw.fitnesssystem.iam.authentication.interfaces.web.dto.request.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/authn")
public class AuthenticationController {

    private final LoginWebAssembler loginWebAssembler;
    private final LogoutWebAssembler logoutWebAssembler;
    private final RefreshWebAssembler refreshWebAssembler;
    private final LoginCommandService loginCommandService;
    private final LogoutCommandService logoutCommandService;
    private final TokenRefreshCommandService tokenRefreshCommandService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
        @RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest
    ) {
        // 1. Web层：提取Web特有数据（IP、User-Agent），构建Application层的输入Command
        LoginCommand command = loginWebAssembler.toCommand(request, httpRequest);

        // 2. 调用Application层（核心业务逻辑），得到纯业务输出
        LoginResult result = loginCommandService.login(command);

        // 3. 防腐层转换：将业务输出（LoginResult）转换为协议输出（LoginResponse）
        LoginResponse response = loginWebAssembler.toResponse(result);

        return ApiResponse.success(response);
    }

    /**
     * 退出当前登录
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        LogoutCommand command = logoutWebAssembler.toCommand();
        logoutCommandService.logout(command);
        return ApiResponse.success();
    }

    /**
     * 刷新Token，实现双Token轮换
     *
     * @param request 刷新请求体
     * @param httpRequest HttpServletRequest请求体
     * @return 刷新响应体
     */
    @PostMapping("/refresh")
    public ApiResponse<RefreshResponse> refresh(
        @RequestBody @Valid RefreshRequest request,  HttpServletRequest httpRequest
    ) {
        RefreshCommand command = refreshWebAssembler.toCommand(request, httpRequest);
        RefreshResult result = tokenRefreshCommandService.refresh(command);
        RefreshResponse response = refreshWebAssembler.toResponse(result);
        return ApiResponse.success(response);
    }

}
