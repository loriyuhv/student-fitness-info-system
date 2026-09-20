package com.wsw.fitnesssystem.user.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * user 模块错误码。
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 18:34
 * @since 1.0
 */
public enum UserErrorCode implements ErrorCode {

    /* ================= 用户 / 账号 ================= */
    USER_NOT_FOUND(404001, HttpStatus.NOT_FOUND, "用户不存在"),
    USER_ALREADY_EXIST(409003, HttpStatus.CONFLICT, "用户已存在"),
    ACCOUNT_NOT_EXIST(404002, HttpStatus.NOT_FOUND, "账号不存在"),

    /* ================= 唯一约束冲突 ================= */
    PHONE_ALREADY_EXISTS(409201, HttpStatus.CONFLICT, "手机号已被使用"),
    EMAIL_ALREADY_EXISTS(409202, HttpStatus.CONFLICT, "邮箱已被使用"),
    USERNAME_ALREADY_EXISTS(409203, HttpStatus.CONFLICT, "用户名已被占用");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    UserErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override public int code()       { return code; }
    @Override public String message() { return message; }

    public HttpStatus httpStatus() { return httpStatus; }

}
