package com.wsw.fitnesssystem.auth.infrastructure.security.handler;

import com.wsw.fitnesssystem.auth.infrastructure.security.support.SecurityResponseWriter;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 权限不足处理器（403）
 * 触发时机：用户已通过认证（Token 合法），但不具备访问当前接口所需的角色 / 权限
 * 设计原则：不关心用户是谁、权限如何计算，只负责权限校验失败后的统一响应，
 * 避免抛出异常影响 Filter 链。
 * 返回结果：
 * - HTTP Status：403
 * - 业务状态码：{@link ResultCode#PERMISSION_DENIED}
 *
 * @author loriyuhv
 * @version 1.0 2026/1/15 0:11
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    private final SecurityResponseWriter responseWriter;

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException) throws IOException {

        ResultCode resultCode = ResultCode.PERMISSION_DENIED;

        responseWriter.write(response, resultCode);
    }
}
