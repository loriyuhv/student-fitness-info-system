package com.wsw.fitnesssystem.identity.infrastructure.security.support;

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
 * @author loriyuhv
 * @version 1.0 2026/1/15 0:18
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class SecurityResponseWriter {
    private final ObjectMapper objectMapper;

    public void write(
        HttpServletResponse response,
        ResultCode resultCode) throws IOException {

        response.setStatus(resultCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ApiResult<Object> result = ApiResult.error(resultCode);
        objectMapper.writeValue(response.getWriter(), result);
    }
}
