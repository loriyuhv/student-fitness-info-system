package com.wsw.fitnesssystem.auth.authentication.infrastructure.security.handler;

import com.wsw.fitnesssystem.auth.authentication.infrastructure.security.support.SecurityResponseWriter;
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
 * JWT 过滤器链内权限不足处理器（403）
 * <p>【重要触发边界】仅处理 <b>Filter 链路内部</b>产生的 AccessDeniedException：
 * <li>1. SecurityFilterChain #authorizeHttpRequests 路径匹配鉴权失败</li>
 * <li>2. 自定义Filter中手动抛出 AccessDeniedException</li>
 * <p>⚠️ 注意：Controller {@code @PreAuthorize} 方法鉴权失败属于AOP层异常，不会进入此类，由全局异常处理器捕获。
 *
 * <p>触发前提：Token合法、用户认证成功（已登录），但是缺少访问资源所需角色/权限。
 * 响应规范：
 * <ul>
 *     <li>HTTP Status：403 FORBIDDEN</li>
 *     <li>业务码：{@link ResultCode#PERMISSION_DENIED}</li>
 * </ul>
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
        AccessDeniedException accessDeniedException
    ) throws IOException {

        ResultCode resultCode = ResultCode.PERMISSION_DENIED;
        log.warn("已认证用户访问受限资源，权限校验不通过 | URI: {} | 原因: {}",
                request.getRequestURI(),
                accessDeniedException.getMessage()
        );
        responseWriter.write(response, resultCode);
    }
}
