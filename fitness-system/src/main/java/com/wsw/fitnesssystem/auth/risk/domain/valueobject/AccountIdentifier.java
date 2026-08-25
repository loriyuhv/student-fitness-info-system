package com.wsw.fitnesssystem.auth.risk.domain.valueobject;

import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;

/**
 * 账号标识 - 值对象
 *
 * <p>风控场景下，登录前可能只有 username，没有 userId/campusId。
 * 用专门的值对象比通用的 {@code Operator} 更精确。</p>
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:01
 * @since 1.0
 */
public record AccountIdentifier(String username) {
    public AccountIdentifier {
        if (username == null || username.isBlank()) {
            throw new BizException(ResultCode.PARAM_INVALID);
        }
    }
}
