package com.wsw.fitnesssystem.iam.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;

/**
 * iam.risk 子域错误码（登录风控）。
 *
 * <p><b>命名规范：</b>{@code iam.risk.*}。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 17:58
 * @since 1.0
 */
public enum IamRiskErrorCode implements ErrorCode {

    ACCOUNT_LOCKED("iam.risk.account_locked", "账号已被锁定"),
    ACCOUNT_DISABLED("iam.risk.account_disabled", "账号已被禁用"),
    FAIL_THRESHOLD_EXCEEDED("iam.risk.fail_threshold_exceeded", "失败次数已达上限"),
    CHECK_FAILED("iam.risk.check_failed", "风控检查不通过");

    private final String code;
    private final String message;

    IamRiskErrorCode(String code, String message) {
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
