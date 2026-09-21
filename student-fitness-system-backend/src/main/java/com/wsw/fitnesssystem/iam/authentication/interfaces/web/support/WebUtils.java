package com.wsw.fitnesssystem.iam.authentication.interfaces.web.support;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

/**
 * Web工具类
 *
 * @author loriyuhv
 * @version 1.0 2026/8/25 22:44
 * @since 1.0
 */
public class WebUtils {

    private static final String UNKNOWN = "unknown";

    private static final String[] IP_HEADERS = {
        "X-Forwarded-For",
        "X-Real-IP",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_CLIENT_IP",
        "HTTP_X_FORWARDED_FOR"
    };

    private WebUtils() {}

    /**
     * 获取客户端IP地址
     * @param request 请求
     * @return 实际IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                // X-Forwarded-For 可能是 "client, proxy1, proxy2"
                int comma = ip.indexOf(',');
                return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
            }
        }
        return request.getRemoteAddr();
    }

    private static boolean isValidIp(String ip) {
        return StringUtils.isNotBlank(ip) && !UNKNOWN.equalsIgnoreCase(ip);
    }

}
