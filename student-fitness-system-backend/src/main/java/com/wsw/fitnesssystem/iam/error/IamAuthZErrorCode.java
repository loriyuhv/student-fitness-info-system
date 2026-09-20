package com.wsw.fitnesssystem.iam.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * iam.authorization 子域错误码（权限 / 角色）。
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 17:57
 * @since 1.0
 */
public enum IamAuthZErrorCode implements ErrorCode {

    /* ================= 权限 ================= */
    PERMISSION_DENIED(403001, HttpStatus.FORBIDDEN, "权限不足"),
    ROLE_NOT_ASSIGNED(403002, HttpStatus.FORBIDDEN, "未分配角色"),
    PERMISSION_EXPIRED(403003, HttpStatus.FORBIDDEN, "权限已过期"),

    /* ================= 唯一约束 ================= */
    ROLE_CODE_ALREADY_EXISTS(409204, HttpStatus.CONFLICT, "角色编码已存在"),
    ROLE_NAME_ALREADY_EXISTS(409205, HttpStatus.CONFLICT, "角色名称已存在"),
    PERM_CODE_ALREADY_EXISTS(409206, HttpStatus.CONFLICT, "权限编码已存在"),
    PERM_NAME_ALREADY_EXISTS(409207, HttpStatus.CONFLICT, "权限名称已存在");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    IamAuthZErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public int code() {
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
