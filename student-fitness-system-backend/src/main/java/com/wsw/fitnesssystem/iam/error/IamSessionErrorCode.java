package com.wsw.fitnesssystem.iam.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * iam.session 子域错误码（会话管理）。
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 17:53
 * @since 1.0
 */
public enum IamSessionErrorCode implements ErrorCode {

    SESSION_ALREADY_OFFLINE(400201, HttpStatus.BAD_REQUEST, "会话已下线"),
    SESSION_NOT_FOUND(404201, HttpStatus.NOT_FOUND, "会话不存在"),
    SESSION_TOKEN_INVALID(401201, HttpStatus.UNAUTHORIZED, "会话Token无效"),
    SESSION_MAX_DEVICES_EXCEEDED(403201, HttpStatus.FORBIDDEN, "超过最大登录设备数");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    IamSessionErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override public int code()       { return code; }
    @Override public String message() { return message; }

    public HttpStatus httpStatus() { return httpStatus; }

}
