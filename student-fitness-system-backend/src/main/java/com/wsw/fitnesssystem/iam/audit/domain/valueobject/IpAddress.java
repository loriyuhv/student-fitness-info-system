package com.wsw.fitnesssystem.iam.audit.domain.valueobject;

import com.wsw.fitnesssystem.shared.application.exception.BizException;
import com.wsw.fitnesssystem.shared.interfaces.web.response.ErrorCode;

/**
 * IP 地址
 *
 * @author loriyuhv
 * @version 1.0 2026/8/25 11:40
 * @since 1.0
 */
public record IpAddress(String value) {
    public IpAddress {
        if (value == null || value.isBlank()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "IP cannot be blank");
        }
    }
}
