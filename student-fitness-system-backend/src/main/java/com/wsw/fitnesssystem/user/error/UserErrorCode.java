package com.wsw.fitnesssystem.user.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;

/**
 * user 模块错误码。
 *
 * <p><b>职责：</b>承载用户档案、账号基础信息、唯一约束相关的错误码。</p>
 *
 * <p><b>覆盖范围：</b></p>
 * <ul>
 *   <li><b>资源不存在：</b>用户不存在、账号不存在</li>
 *   <li><b>资源已存在：</b>用户已存在</li>
 *   <li><b>唯一约束冲突：</b>手机号、邮箱、用户名</li>
 * </ul>
 *
 * <p><b>命名规范：</b>{@code user.*}</p>
 *
 * <p><b>HTTP 映射：</b>由接口层 {@code HttpStatusResolver} 显式注册，
 * 本枚举不感知 HTTP 语义。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 18:34
 * @since 1.0
 */
public enum UserErrorCode implements ErrorCode {

    /* ================= 用户 / 账号 ================= */

    /** 用户不存在 */
    USER_NOT_FOUND("user.not_found", "用户不存在"),

    /** 用户已存在 */
    USER_ALREADY_EXIST("user.already_exist", "用户已存在"),

    /** 账号不存在 */
    ACCOUNT_NOT_EXIST("user.account_not_exist", "账号不存在"),

    /* ================= 唯一约束冲突 ================= */

    /** 手机号已被使用 */
    PHONE_ALREADY_EXISTS("user.phone.already_exists", "手机号已被使用"),

    /** 邮箱已被使用 */
    EMAIL_ALREADY_EXISTS("user.email.already_exists", "邮箱已被使用"),

    /** 用户名已被占用 */
    USERNAME_ALREADY_EXISTS("user.username.already_exists", "用户名已被占用");

    private final String code;
    private final String message;

    UserErrorCode(String code, String message) {
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
