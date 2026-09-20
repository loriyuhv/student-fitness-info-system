package com.wsw.fitnesssystem.iam.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * iam.risk 子域错误码（登录风控）。
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 18:04
 * @since 1.0
 */
public enum IamRiskErrorCode implements ErrorCode {

    ACCOUNT_LOCKED(403101, HttpStatus.FORBIDDEN, "账号已被锁定"),
    ACCOUNT_DISABLED(403102, HttpStatus.FORBIDDEN, "账号已被禁用"),
    FAIL_THRESHOLD_EXCEEDED(403103, HttpStatus.FORBIDDEN, "失败次数已达上限"),
    CHECK_FAILED(403105, HttpStatus.FORBIDDEN, "风控检查不通过");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    IamRiskErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override public int code()       { return code; }
    @Override public String message() { return message; }

    public HttpStatus httpStatus() { return httpStatus; }

}
