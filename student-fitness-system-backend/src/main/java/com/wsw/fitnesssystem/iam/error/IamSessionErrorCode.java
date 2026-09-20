package com.wsw.fitnesssystem.iam.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;

/**
 * iam 会话子域错误码（iam.session）。
 *
 * <p><b>职责：</b>承载会话生命周期、多端登录控制相关的错误码。</p>
 *
 * <p><b>覆盖范围：</b></p>
 * <ul>
 *   <li><b>会话状态：</b>已下线、不存在</li>
 *   <li><b>会话令牌：</b>Token 无效</li>
 *   <li><b>多端管控：</b>超过最大登录设备数</li>
 * </ul>
 *
 * <p><b>命名规范：</b>{@code iam.session.*}</p>
 *
 * <p><b>HTTP 映射：</b>由接口层 {@code HttpStatusResolver} 显式注册，
 * 本枚举不感知 HTTP 语义。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 17:53
 * @since 1.0
 */
public enum IamSessionErrorCode implements ErrorCode {

    /** 会话已下线（用户主动登出或被动踢出后仍访问） */
    SESSION_ALREADY_OFFLINE("iam.session.already_offline", "会话已下线"),

    /** 会话不存在 */
    SESSION_NOT_FOUND("iam.session.not_found", "会话不存在"),

    /** 会话 Token 无效 */
    SESSION_TOKEN_INVALID("iam.session.token_invalid", "会话Token无效"),

    /** 超过最大登录设备数（受 max-online-sessions 配置限制） */
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