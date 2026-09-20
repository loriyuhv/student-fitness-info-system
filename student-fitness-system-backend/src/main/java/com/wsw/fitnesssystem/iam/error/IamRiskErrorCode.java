package com.wsw.fitnesssystem.iam.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;

/**
 * iam 风控子域错误码（iam.risk）。
 *
 * <p><b>职责：</b>承载登录风控、账号锁定、限流相关的错误码。</p>
 *
 * <p><b>覆盖范围：</b></p>
 * <ul>
 *   <li><b>账号状态：</b>已锁定、已禁用</li>
 *   <li><b>风控触发：</b>失败次数超限、风控检查不通过</li>
 * </ul>
 *
 * <p><b>命名规范：</b>{@code iam.risk.*}</p>
 *
 * <p><b>HTTP 映射：</b>由接口层 {@code HttpStatusResolver} 显式注册，
 * 本枚举不感知 HTTP 语义。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 17:58
 * @since 1.0
 */
public enum IamRiskErrorCode implements ErrorCode {

    /** 账号已被锁定（连续登录失败触发） */
    ACCOUNT_LOCKED("iam.risk.account_locked", "账号已被锁定"),

    /** 账号已被禁用（管理员操作或违规封禁） */
    ACCOUNT_DISABLED("iam.risk.account_disabled", "账号已被禁用"),

    /** 失败次数已达上限（滑动窗口内） */
    FAIL_THRESHOLD_EXCEEDED("iam.risk.fail_threshold_exceeded", "失败次数已达上限"),

    /** 风控检查不通过（IP、设备指纹等综合判定） */
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
