package com.wsw.fitnesssystem.user.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;

/**
 * user 模块错误码。
 *
 * <p><b>命名规范：</b>{@code user.*}。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 18:34
 * @since 1.0
 */
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND("user.not_found", "用户不存在"),
    USER_ALREADY_EXIST("user.already_exist", "用户已存在"),
    ACCOUNT_NOT_EXIST("user.account_not_exist", "账号不存在"),
    PHONE_ALREADY_EXISTS("user.phone.already_exists", "手机号已被使用"),
    EMAIL_ALREADY_EXISTS("user.email.already_exists", "邮箱已被使用"),
    USERNAME_ALREADY_EXISTS("user.username.already_exists", "用户名已被占用");

    private final String code;
    private final String message;

    UserErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

}
