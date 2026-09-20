package com.wsw.fitnesssystem.iam.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;

/**
 * iam 认证子域错误码（iam.authentication）。
 *
 * <p><b>职责：</b>承载 iam 认证相关的错误码：登录、凭证、Token、RefreshToken。</p>
 *
 * <p><b>覆盖范围：</b></p>
 * <ul>
 *   <li><b>认证：</b>账号不存在、密码错误、未登录、凭证无效/过期、账号已存在</li>
 *   <li><b>认证成功：</b>登出成功、踢人成功、账号解锁</li>
 *   <li><b>认证失败：</b>登出失败、踢人失败</li>
 *   <li><b>Token：</b>无效、过期、签名错误、格式错误、版本失效、黑名单</li>
 *   <li><b>RefreshToken：</b>无效、过期</li>
 * </ul>
 *
 * <p><b>命名规范：</b>{@code iam.authn.*} / {@code iam.token.*} / {@code iam.refresh_token.*}</p>
 *
 * <p><b>HTTP 映射：</b>由接口层 {@code HttpStatusResolver} 显式注册，
 * 本枚举不感知 HTTP 语义。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 17:52
 * @since 1.0
 */
public enum IamAuthNErrorCode implements ErrorCode {

    /* ================= 认证 / 登录 ================= */

    /** 认证用户账号不存在（对外统一为「账号或密码错误」防枚举） */
    ACCOUNT_NOT_EXIST("iam.authn.account_not_exist", "认证用户账号不存在"),

    /** 认证用户密码错误（对外统一为「账号或密码错误」防枚举） */
    PASSWORD_ERROR("iam.authn.password_error", "认证用户密码错误"),

    /** 用户未登录（无有效 RequestContext） */
    USER_NOT_LOGIN("iam.authn.user_not_login", "认证用户未登录"),

    /** 登录凭证无效（Token 校验失败或 RequestContext 缺失） */
    CREDENTIAL_INVALID("iam.authn.credential_invalid", "认证用户登录凭证无效"),

    /** 认证用户不存在 */
    USER_NOT_FOUND("iam.authn.user_not_found", "认证用户不存在"),

    /** 登录凭证已过期 */
    CREDENTIAL_EXPIRED("iam.authn.credential_expired", "认证用户登录凭证过期"),

    /** 登录失败（对外统一文案，不暴露具体原因） */
    USER_LOGIN_ERROR("iam.authn.user_login_error", "账号或密码错误"),

    /** 认证账号已存在（注册时用户名/手机号冲突） */
    ACCOUNT_ALREADY_EXIST("iam.authn.account_already_exist", "认证账号已存在"),

    /* ================= 认证成功 ================= */

    /** 用户登出成功 */
    LOGOUT_SUCCESS("iam.authn.logout_success", "用户登出成功"),

    /** 用户被踢出成功（管理员操作） */
    KICKOUT_SUCCESS("iam.authn.kickout_success", "用户被踢出成功"),

    /** 账号已解锁（风控解锁成功） */
    ACCOUNT_UNLOCKED("iam.authn.account_unlocked", "账号已解锁"),

    /* ================= 认证失败 ================= */

    /** 用户登出失败（如 Redis 操作异常） */
    LOGOUT_FAILED("iam.authn.logout_failed", "用户登出失败"),

    /** 用户被踢出失败 */
    KICKOUT_FAILED("iam.authn.kickout_failed", "用户被踢出失败"),

    /* ================= Token ================= */

    /** Token 无效 */
    TOKEN_INVALID("iam.token.invalid", "Token无效"),

    /** Token 已过期 */
    TOKEN_EXPIRED("iam.token.expired", "Token已过期"),

    /** Token 签名错误（被篡改或密钥不匹配） */
    TOKEN_SIGNATURE_ERROR("iam.token.signature_error", "Token签名错误"),

    /** Token 格式错误（不符合 JWT 规范） */
    TOKEN_MALFORMED("iam.token.malformed", "Token格式错误"),

    /** Token 版本失效（用户修改密码或强制下线后） */
    TOKEN_VERSION_MISMATCH("iam.token.version_mismatch", "Token版本失效"),

    /** Token 已加入黑名单（主动登出后） */
    TOKEN_BLACKLISTED("iam.token.blacklisted", "Token已加入黑名单"),

    /* ================= RefreshToken ================= */

    /** RefreshToken 无效 */
    REFRESH_TOKEN_INVALID("iam.refresh_token.invalid", "RefreshToken无效"),

    /** RefreshToken 已过期 */
    REFRESH_TOKEN_EXPIRED("iam.refresh_token.expired", "RefreshToken已过期");

    private final String code;
    private final String message;

    IamAuthNErrorCode(String code, String message) {
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