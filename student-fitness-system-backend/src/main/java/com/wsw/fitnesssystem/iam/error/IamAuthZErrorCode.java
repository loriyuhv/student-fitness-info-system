package com.wsw.fitnesssystem.iam.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;

/**
 * iam 授权子域错误码（iam.authorization）。
 *
 * <p><b>职责：</b>承载权限、角色、数据范围相关的错误码。</p>
 *
 * <p><b>覆盖范围：</b></p>
 * <ul>
 *   <li><b>权限：</b>权限不足、未分配角色、权限已过期</li>
 *   <li><b>唯一约束：</b>角色编码/名称、权限编码/名称冲突</li>
 * </ul>
 *
 * <p><b>命名规范：</b>{@code iam.authz.*}</p>
 *
 * <p><b>HTTP 映射：</b>由接口层 {@code HttpStatusResolver} 显式注册，
 * 本枚举不感知 HTTP 语义。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 17:57
 * @since 1.0
 */
public enum IamAuthZErrorCode implements ErrorCode {

    /* ================= 权限 ================= */

    /** 权限不足（角色不匹配或功能权限缺失） */
    PERMISSION_DENIED("iam.authz.permission_denied", "权限不足"),

    /** 未分配角色（用户无任何角色） */
    ROLE_NOT_ASSIGNED("iam.authz.role_not_assigned", "未分配角色"),

    /** 权限已过期 */
    PERMISSION_EXPIRED("iam.authz.permission_expired", "权限已过期"),

    /* ================= 唯一约束 ================= */

    /** 角色编码已存在 */
    ROLE_CODE_ALREADY_EXISTS("iam.authz.role_code_already_exists", "角色编码已存在"),

    /** 角色名称已存在 */
    ROLE_NAME_ALREADY_EXISTS("iam.authz.role_name_already_exists", "角色名称已存在"),

    /** 权限编码已存在 */
    PERM_CODE_ALREADY_EXISTS("iam.authz.perm_code_already_exists", "权限编码已存在"),

    /** 权限名称已存在 */
    PERM_NAME_ALREADY_EXISTS("iam.authz.perm_name_already_exists", "权限名称已存在");

    private final String code;
    private final String message;

    IamAuthZErrorCode(String code, String message) {
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
