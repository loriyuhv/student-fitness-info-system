package com.wsw.fitnesssystem.auth.authentication.infrastructure.security.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wsw.fitnesssystem.shared.response.ApiResult;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Security模块统一响应输出工具
 * <li>收拢过滤器、安全处理器内JSON响应输出逻辑，消除重复Header设置、序列化代码</li>
 * <li>统一使用项目标准 {@link ApiResult} 返回结构，保证前后端格式一致</li>
 * <li>复用全局ObjectMapper，序列化行为与Controller层保持统一</li>
 * 注意：仅用于Security Filter链内组件输出响应，不要在Controller中使用
 * @author loriyuhv
 * @version 1.0 2026/1/15 0:18
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class SecurityResponseWriter {

    private final ObjectMapper objectMapper;

    /**
     * 根据错误码输出标准失败响应，使用ResultCode内置提示信息
     * @param response http响应对象
     * @param resultCode 业务结果码
     * @throws IOException 输入输出异常
     */
    public void write(HttpServletResponse response, ResultCode resultCode) throws IOException {
        write(response, resultCode, resultCode.getMessage());
    }

    /**
     * 根据错误码 + 自定义消息输出响应
     * @param response http响应对象
     * @param resultCode 业务结果状态码
     * @param message 自定义提示文本
     * @throws IOException 输入输出异常
     */
    public void write(
        HttpServletResponse response,
        ResultCode resultCode,
        String message
    ) throws IOException {

        response.setStatus(resultCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ApiResult<Object> result;
        if (message != null && !message.isBlank()) {
            result = ApiResult.error(resultCode, message);
        } else {
            result = ApiResult.error(resultCode);
        }
        objectMapper.writeValue(response.getWriter(), result);
    }
}
