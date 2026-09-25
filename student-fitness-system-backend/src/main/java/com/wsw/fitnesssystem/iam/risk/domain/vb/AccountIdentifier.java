package com.wsw.fitnesssystem.iam.risk.domain.vb;

import com.wsw.fitnesssystem.shared.domain.exception.DomainValidationException;
import org.apache.commons.lang3.StringUtils;

/**
 * 账号标识 - 值对象
 *
 * <p>风控场景下，登录前可能只有 username，没有 userId/campusId。
 * 用专门的值对象比通用的 {@code Operator} 更精确。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:01
 * @since 1.0
 */
public record AccountIdentifier(String username) {

    public AccountIdentifier {
        if (StringUtils.isBlank(username)) {
            throw new DomainValidationException("username 不能为空");
        }
    }

}
