package com.wsw.fitnesssystem.auth.authentication.application.dto.port;

import lombok.Builder;
import lombok.Data;

/**
 * 认证用户凭证（Auth 模块定义的端口契约）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>Auth 模块对"认证数据"的契约定义</li>
 *   <li>作为 {@code AuthUserDataProvider} 接口的返回类型</li>
 *   <li><b>注意：</b>此 DTO 属于 Auth 模块的端口契约，
 *       与 User 模块的 {@code UserAuthData} 是两回事</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 14:14
 * @since 1.0
 */
@Data
@Builder
public class AuthUserCredential {

    private Long userId;

    private Long campusId;

    private String username;

    /** 密码哈希（BCrypt 密文） */
    private String password;

    /** 0-管理员, 1-教师, 2-学生 */
    private Integer userType;

    /** 0-禁用, 1-启用 */
    private Integer status;

}
