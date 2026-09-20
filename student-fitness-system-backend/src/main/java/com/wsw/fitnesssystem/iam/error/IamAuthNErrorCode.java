package com.wsw.fitnesssystem.iam.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * iam.authentication 子域错误码（认证 / Token）。
 *
 * <p><b>命名规范：</b>{@code iam.authn.*}（认证）/ {@code iam.token.*}（访问令牌）
 * / {@code iam.refresh_token.*}（刷新令牌）。</p>
 *
 * <p><b>P5 变更：</b>code 从数字迁移为字符串码，与 HTTP 语义解耦。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 17:52
 * @since 1.0
 */
public enum IamAuthNErrorCode implements ErrorCode {

    /* ================= 认证 / 登录 ================= */
    ACCOUNT_NOT_EXIST("iam.authn.account_not_exist", HttpStatus.UNAUTHORIZED, "认证用户账号不存在"),
    PASSWORD_ERROR("iam.authn.password_error", HttpStatus.UNAUTHORIZED, "认证用户密码错误"),
    USER_NOT_LOGIN("iam.authn.user_not_login", HttpStatus.UNAUTHORIZED, "认证用户未登录"),
    CREDENTIAL_INVALID("iam.authn.credential_invalid", HttpStatus.UNAUTHORIZED, "认证用户登录凭证无效"),
    USER_NOT_FOUND("iam.authn.user_not_found", HttpStatus.UNAUTHORIZED, "认证用户不存在"),
    CREDENTIAL_EXPIRED("iam.authn.credential_expired", HttpStatus.UNAUTHORIZED, "认证用户登录凭证过期"),
    USER_LOGIN_ERROR("iam.authn.user_login_error", HttpStatus.UNAUTHORIZED, "账号或密码错误"),
    ACCOUNT_ALREADY_EXIST("iam.authn.account_already_exist", HttpStatus.CONFLICT, "认证账号已存在"),

    /* ================= 成功（认证相关） ================= */
    LOGOUT_SUCCESS("iam.authn.logout_success", HttpStatus.OK, "用户登出成功"),
    KICKOUT_SUCCESS("iam.authn.kickout_success", HttpStatus.OK, "用户被踢出成功"),
    ACCOUNT_UNLOCKED("iam.authn.account_unlocked", HttpStatus.OK, "账号已解锁"),

    /* ================= 认证操作失败 ================= */
    LOGOUT_FAILED("iam.authn.logout_failed", HttpStatus.INTERNAL_SERVER_ERROR, "用户登出失败"),
    KICKOUT_FAILED("iam.authn.kickout_failed", HttpStatus.INTERNAL_SERVER_ERROR, "用户被踢出失败"),

    /* ================= Token ================= */
    TOKEN_INVALID("iam.token.invalid", HttpStatus.UNAUTHORIZED, "Token无效"),
    TOKEN_EXPIRED("iam.token.expired", HttpStatus.UNAUTHORIZED, "Token已过期"),
    TOKEN_SIGNATURE_ERROR("iam.token.signature_error", HttpStatus.UNAUTHORIZED, "Token签名错误"),
    TOKEN_MALFORMED("iam.token.malformed", HttpStatus.UNAUTHORIZED, "Token格式错误"),
    TOKEN_VERSION_MISMATCH("iam.token.version_mismatch", HttpStatus.UNAUTHORIZED, "Token版本失效"),
    TOKEN_BLACKLISTED("iam.token.blacklisted", HttpStatus.UNAUTHORIZED, "Token已加入黑名单"),

    /* ================= RefreshToken ================= */
    REFRESH_TOKEN_INVALID("iam.refresh_token.invalid", HttpStatus.UNAUTHORIZED, "RefreshToken无效"),
    REFRESH_TOKEN_EXPIRED("iam.refresh_token.expired", HttpStatus.UNAUTHORIZED, "RefreshToken已过期");

    private final String code;
    private final HttpStatus httpStatus;   // P6 移除
    private final String message;

    IamAuthNErrorCode(String code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
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

    public HttpStatus httpStatus() {
        return httpStatus;
    }

}