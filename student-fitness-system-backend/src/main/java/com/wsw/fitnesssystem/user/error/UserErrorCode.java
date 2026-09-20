package com.wsw.fitnesssystem.user.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;
import org.springframework.http.HttpStatus;

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

    /* ================= 用户 / 账号 ================= */
    USER_NOT_FOUND("user.not_found", HttpStatus.NOT_FOUND, "用户不存在"),
    USER_ALREADY_EXIST("user.already_exist", HttpStatus.CONFLICT, "用户已存在"),
    ACCOUNT_NOT_EXIST("user.account_not_exist", HttpStatus.NOT_FOUND, "账号不存在"),

    /* ================= 唯一约束冲突 ================= */
    PHONE_ALREADY_EXISTS("user.phone.already_exists", HttpStatus.CONFLICT, "手机号已被使用"),
    EMAIL_ALREADY_EXISTS("user.email.already_exists", HttpStatus.CONFLICT, "邮箱已被使用"),
    USERNAME_ALREADY_EXISTS("user.username.already_exists", HttpStatus.CONFLICT, "用户名已被占用");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    UserErrorCode(String code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
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

    public HttpStatus httpStatus() {
        return httpStatus;
    }

}
