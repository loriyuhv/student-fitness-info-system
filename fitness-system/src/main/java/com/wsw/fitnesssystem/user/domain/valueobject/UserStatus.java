package com.wsw.fitnesssystem.user.domain.valueobject;

import lombok.Getter;

/**
 * 用户状态（业务可见性）
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 12:26
 * @since 1.0
 */
@Getter
public enum UserStatus {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final int code;
    private final String desc;

    UserStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserStatus of(int code) {
        for (UserStatus status : values()) {
            if (status.code == code) return status;
        }
        throw new IllegalArgumentException("Unknown user locked: " + code);
    }

}
