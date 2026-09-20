package com.wsw.fitnesssystem.iam.authentication.infrastructure.security.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wsw.fitnesssystem.shared.interfaces.web.exception.GlobalExceptionHandler;
import com.wsw.fitnesssystem.shared.interfaces.web.exception.HttpStatusResolver;
import com.wsw.fitnesssystem.shared.interfaces.web.response.ApiResult;
import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Spring Security 异常响应写出器。
 *
 * <p><b>职责：</b>Spring Security 的认证/授权异常不走 {@code @RestControllerAdvice}，
 * 需要由 {@code AuthenticationEntryPoint} / {@code AccessDeniedHandler}
 * 通过本类直接写出响应。输出格式必须与 {@link GlobalExceptionHandler}
 * 保持一致。</p>
 *
 * <p><b>当前阶段（P3）：</b>HTTP 状态固定 200，真实状态放在 body 的 {@code httpCode} 字段。
 * P6 阶段将统一改为真实 HTTP 状态。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/15 0:18
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityResponseWriter {

    private final ObjectMapper objectMapper;
    private final HttpStatusResolver httpStatusResolver;

    /**
     * 使用错误码的默认提示信息写出响应。
     *
     * @param response http响应对象
     * @param errorCode 业务结果码
     * @throws IOException 输入输出异常
     */
    public void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        write(response, errorCode, errorCode.message());
    }

    /**
     * 使用自定义提示信息写出响应。
     *
     * @param response http响应对象
     * @param errorCode 业务结果状态码
     * @param message 自定义提示文本
     * @throws IOException 输入输出异常
     */
    public void write(
        HttpServletResponse response,
        ErrorCode errorCode,
        String message
    ) throws IOException {
        // P3 阶段：与 GlobalExceptionHandler 保持一致，HTTP 状态固定 200
        // P6 阶段：改为 httpStatusResolver.resolve(errorCode).value()
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        int httpCode = httpStatusResolver.resolveValue(errorCode);
        String finalMsg = StringUtils.isNotBlank(message) ? errorCode.message() : message;
        ApiResult<Object> result = ApiResult.error(httpCode, errorCode, finalMsg);

        log.debug("[Security] 写出异常响应：httpCode={}, bizCode={}, msg={}",
            httpCode, errorCode.code(), finalMsg);

        objectMapper.writeValue(response.getWriter(), result);
    }

}
