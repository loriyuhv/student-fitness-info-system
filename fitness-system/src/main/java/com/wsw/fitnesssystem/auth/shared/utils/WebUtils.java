package com.wsw.fitnesssystem.auth.shared.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Web工具类
 *
 * @author loriyuhv
 * @version 1.0 2026/8/25 22:44
 * @since 1.0
 */
public class WebUtils {

    /**
     * 获取客户端IP地址
     * @param request 请求
     * @return 实际IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
