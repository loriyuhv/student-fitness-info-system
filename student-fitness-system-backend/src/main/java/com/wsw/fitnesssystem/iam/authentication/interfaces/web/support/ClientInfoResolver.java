package com.wsw.fitnesssystem.iam.authentication.interfaces.web.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 从 HttpServletRequest 中解析客户端信息。
 * 属于 interfaces 层 Web 适配器，依赖 servlet API。
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21 12:26
 * @since 1.0
 */
@Component
public class ClientInfoResolver {

    private static final String HEADER_DEVICE_ID = "X-Device-Id";
    private static final String HEADER_DEVICE_TYPE = "X-Device-Type";
    private static final String HEADER_USER_AGENT = "User-Agent";

    public ClientInfo resolve(HttpServletRequest request) {
        return new ClientInfo(
            request.getHeader(HEADER_DEVICE_ID),
            request.getHeader(HEADER_DEVICE_TYPE),
            WebUtils.getClientIp(request),
            request.getHeader(HEADER_USER_AGENT)
        );
    }

}
