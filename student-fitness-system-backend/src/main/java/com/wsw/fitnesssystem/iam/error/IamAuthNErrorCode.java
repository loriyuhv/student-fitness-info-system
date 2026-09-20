package com.wsw.fitnesssystem.iam.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * iam.authentication 子域错误码（认证 / Token）。
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 17:52
 * @since 1.0
 */
public enum IamAuthNErrorCode implements ErrorCode {

    /* ================= 认证 / 登录 ================= */
    ACCOUNT_NOT_EXIST(401001, HttpStatus.UNAUTHORIZED, "认证用户账号不存在"),
    PASSWORD_ERROR(401002, HttpStatus.UNAUTHORIZED, "认证用户密码错误"),
    USER_NOT_LOGIN(401003, HttpStatus.UNAUTHORIZED, "认证用户未登录"),
    CREDENTIAL_INVALID(401004, HttpStatus.UNAUTHORIZED, "认证用户登录凭证无效"),
    USER_NOT_FOUND(401005, HttpStatus.UNAUTHORIZED, "认证用户不存在"),
    CREDENTIAL_EXPIRED(401006, HttpStatus.UNAUTHORIZED, "认证用户登录凭证过期"),
    USER_LOGIN_ERROR(401007, HttpStatus.UNAUTHORIZED, "账号或密码错误"),
    ACCOUNT_ALREADY_EXIST(409001, HttpStatus.CONFLICT, "认证账号已存在"),

    /* ================= 成功（认证相关） ================= */
    LOGOUT_SUCCESS(200101, HttpStatus.OK, "用户登出成功"),
    KICKOUT_SUCCESS(200102, HttpStatus.OK, "用户被踢出成功"),
    ACCOUNT_UNLOCKED(200103, HttpStatus.OK, "账号已解锁"),
    LOGOUT_FAILED(500101, HttpStatus.INTERNAL_SERVER_ERROR, "用户登出失败"),
    KICKOUT_FAILED(500102, HttpStatus.INTERNAL_SERVER_ERROR, "用户被踢出失败"),

    /* ================= Token ================= */
    TOKEN_INVALID(401101, HttpStatus.UNAUTHORIZED, "Token无效"),
    TOKEN_EXPIRED(401102, HttpStatus.UNAUTHORIZED, "Token已过期"),
    TOKEN_SIGNATURE_ERROR(401103, HttpStatus.UNAUTHORIZED, "Token签名错误"),
    TOKEN_MALFORMED(401104, HttpStatus.UNAUTHORIZED, "Token格式错误"),
    TOKEN_VERSION_MISMATCH(401105, HttpStatus.UNAUTHORIZED, "Token版本失效"),
    TOKEN_BLACKLISTED(401106, HttpStatus.UNAUTHORIZED, "Token已加入黑名单"),

    /* ================= RefreshToken ================= */
    REFRESH_TOKEN_INVALID(401111, HttpStatus.UNAUTHORIZED, "RefreshToken无效"),
    REFRESH_TOKEN_EXPIRED(401112, HttpStatus.UNAUTHORIZED, "RefreshToken已过期");

    private final int code;
    private final HttpStatus httpStatus;   // P3 移除
    private final String message;

    IamAuthNErrorCode(int code, HttpStatus httpStatus, String message) {
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

    /**
     * P3 阶段移除
     */
    public HttpStatus httpStatus() {
        return httpStatus;
    }

}
