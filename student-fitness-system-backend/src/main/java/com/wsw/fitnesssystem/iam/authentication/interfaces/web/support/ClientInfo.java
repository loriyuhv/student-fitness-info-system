package com.wsw.fitnesssystem.iam.authentication.interfaces.web.support;

import org.apache.commons.lang3.StringUtils;

/**
 * 客户端信息值对象，来源于 HTTP 请求。
 * 位于 interfaces 层，因为它只服务于 Web 适配器。
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21 12:24
 * @since 1.0
 */
public record ClientInfo(
    String deviceId,
    String deviceType,
    String clientIp,
    String userAgent
) {

    public ClientInfo {
        deviceId = normalize(deviceId);
        deviceType = normalize(deviceType);
        clientIp = normalize(clientIp);
        userAgent = normalize(userAgent);
    }

    private static String normalize(String value) {
        return (StringUtils.isBlank(value)) ? null : value.trim();
    }

}
