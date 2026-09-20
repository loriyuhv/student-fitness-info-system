package com.wsw.fitnesssystem.shared.infrastructure.persistence;

import com.wsw.fitnesssystem.iam.error.IamAuthZErrorCode;
import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;
import com.wsw.fitnesssystem.user.error.UserErrorCode;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MySQL 唯一约束名 → 业务 ErrorCode 映射。
 * <p>约束名来自 information_schema，命名规范：uk_{field}_{suffix}。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/18 15:47
 * @since 1.0
 */
public final class ConstraintResultCodeMapper {

    /** 从 Duplicate entry 'xxx' for key 'table.uk_phone_deleted' 提取约束名 */
    private static final Pattern KEY_PATTERN =
        Pattern.compile("for key '([^.']+\\.)?([^']+)'");

    /** 约束名（不含表前缀）→ 业务错误码 */
    private static final Map<String, ErrorCode> RULES = Map.ofEntries(
        // user_profile
        Map.entry("uk_phone_deleted",       UserErrorCode.PHONE_ALREADY_EXISTS),
        Map.entry("uk_email_deleted",       UserErrorCode.EMAIL_ALREADY_EXISTS),
        // sys_user
        Map.entry("uk_username_deleted",    UserErrorCode.USERNAME_ALREADY_EXISTS),
        // sys_role
        Map.entry("uk_role_code_deleted",   IamAuthZErrorCode.ROLE_CODE_ALREADY_EXISTS),
        Map.entry("uk_role_name_deleted",   IamAuthZErrorCode.ROLE_NAME_ALREADY_EXISTS),
        // sys_permission
        Map.entry("uk_perm_code_deleted",   IamAuthZErrorCode.PERM_CODE_ALREADY_EXISTS),
        Map.entry("uk_perm_name_deleted",   IamAuthZErrorCode.PERM_NAME_ALREADY_EXISTS)
    );

    private ConstraintResultCodeMapper() {}

    /**
     * 尝试把约束名翻译成业务错误码。
     *
     * @param message 底层 SQL 异常消息
     * @return 命中的 ErrorCode；未命中返回 {@code null}
     */
    public static ErrorCode resolve(String message) {
        if (message == null) return null;
        Matcher matcher = KEY_PATTERN.matcher(message);
        if (!matcher.find()) return null;
        String key = matcher.group(2);
        return RULES.get(key);
    }

}
