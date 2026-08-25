package com.wsw.fitnesssystem.auth.domain.audit.valueobject;

import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;

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
            throw new BizException(ResultCode.PARAM_INVALID, "IP cannot be blank");
        }
    }
}
