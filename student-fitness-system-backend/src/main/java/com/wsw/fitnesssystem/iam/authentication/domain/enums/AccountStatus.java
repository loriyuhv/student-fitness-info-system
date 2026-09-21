package com.wsw.fitnesssystem.iam.authentication.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户账号状态（业务可见性）
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 12:26
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum AccountStatus {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final int code;
    private final String desc;

    public static AccountStatus of(int code) {
        for (AccountStatus accountStatus : values()) {
            if (accountStatus.code == code) return accountStatus;
        }
        throw new IllegalArgumentException("无效的账户状态编码：" + code);
    }

}
