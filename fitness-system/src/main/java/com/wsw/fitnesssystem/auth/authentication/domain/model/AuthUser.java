package com.wsw.fitnesssystem.auth.authentication.domain.model;

import com.wsw.fitnesssystem.auth.authentication.application.dto.port.AuthUserCredential;
import com.wsw.fitnesssystem.auth.authentication.domain.port.PasswordEncryptor;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 认证用户聚合根（Auth 模块核心领域模型）
 * <p>只表达「和用户本身有关」的业务规则，不关心登录流程、不关心 JWT、不关心权限。</p>
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>封装密码校验、账号状态检查等认证领域逻辑</li>
 *   <li>只接收 {@code AuthUserCredential}（Auth 模块自己的契约）</li>
 *   <li>不依赖任何外部模块的类（包括 User 模块）</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/15 14:38
 * @since 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {

    private Long userId;
    private Long campusId;
    private String username;
    /** BCrypt 密文 */
    private String passwordHash;
    private Integer userType;
    /** 0-禁用, 1-启用 */
    private Integer status;

    /** 从 Auth 模块的契约创建 AuthUser */
    public static AuthUser loadFromCredential(AuthUserCredential credential) {
        return AuthUser.builder()
            .userId(credential.getUserId())
            .campusId(credential.getCampusId())
            .username(credential.getUsername())
            .passwordHash(credential.getPassword())
            .userType(credential.getUserType())
            .status(credential.getStatus())
            .build();
    }

    /**
     * 检查账号是否启用
     * @return 布尔值
     */
    public boolean isEnabled() {
        return !Integer.valueOf(1).equals(status);
    }

    public void verifyPassword(String rawPassword, PasswordEncryptor passwordEncryptor) {
        // 1. 检查账号状态
        if (isEnabled()) {
            throw new BizException(ResultCode.ACCOUNT_DISABLED);
        }

        // 2. 密码比对
        boolean matches = passwordEncryptor.matches(rawPassword, this.passwordHash);
        if (!matches) {
            throw new BizException(ResultCode.PASSWORD_ERROR);
        }
    }

}
