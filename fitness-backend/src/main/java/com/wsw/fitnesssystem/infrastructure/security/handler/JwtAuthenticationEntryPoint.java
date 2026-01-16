package com.wsw.fitnesssystem.infrastructure.security.handler;

import com.wsw.fitnesssystem.infrastructure.security.support.SecurityResponseWriter;
import com.wsw.fitnesssystem.interfaces.response.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT认证失败处理器
 * 触发时机：
 * - 请求未携带 Token
 * - Token 已过期 / 非法 / 签名错误
 * - 当前请求需要认证，但 SecurityContext 中无 Authentication
 * 设计原则：
 * - 不抛异常（Filter 链路已终止）
 * - 不进入 Controller / GlobalExceptionHandler
 * - 只负责将认证异常转换为统一的接口响应格式
 * 返回结果：
 * - HTTP Status：401
 * - 业务状态码：{@link ResultCode#USER_NOT_LOGIN}
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 23:53
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    /** Spring 管理的单例 */
    private final SecurityResponseWriter securityResponseWriter;

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        ResultCode resultCode =
            authException instanceof CredentialsExpiredException
                ? ResultCode.TOKEN_EXPIRED
                : ResultCode.USER_NOT_LOGIN;

        // 记录安全日志（WARN级别）
        log.debug("认证失败，访问未授权资源: {}，Exception：{}",
            request.getRequestURI(),
            authException.getMessage()
        );

        // 写入响应
        securityResponseWriter.write(response, resultCode);
    }
}
