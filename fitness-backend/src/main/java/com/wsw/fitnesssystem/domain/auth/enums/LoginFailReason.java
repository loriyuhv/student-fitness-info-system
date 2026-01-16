package com.wsw.fitnesssystem.domain.auth.enums;

import lombok.Getter;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/12 4:35
 * @since 1.0
 */
@Getter
public enum LoginFailReason {
    USER_NOT_FOUND("用户不存在"),
    PASSWORD_ERROR("密码错误"),
    ACCOUNT_LOCKED("账号或IP已锁定"),
    ACCOUNT_DISABLED("账号被禁用");

    private final String desc;

    LoginFailReason(String desc) {
        this.desc = desc;
    }
}
