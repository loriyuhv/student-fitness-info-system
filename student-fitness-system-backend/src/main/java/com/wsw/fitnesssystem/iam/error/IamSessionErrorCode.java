package com.wsw.fitnesssystem.iam.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;

/**
 * iam.session 子域错误码（会话管理）。
 *
 * <p><b>命名规范：</b>{@code iam.session.*}。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 17:53
 * @since 1.0
 */
public enum IamSessionErrorCode implements ErrorCode {

    SESSION_ALREADY_OFFLINE("iam.session.already_offline", "会话已下线"),
    SESSION_NOT_FOUND("iam.session.not_found", "会话不存在"),
    SESSION_TOKEN_INVALID("iam.session.token_invalid", "会话Token无效"),
    SESSION_MAX_DEVICES_EXCEEDED("iam.session.max_devices_exceeded", "超过最大登录设备数");

    private final String code;
    private final String message;

    IamSessionErrorCode(String code, String message) {
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