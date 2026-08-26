package com.wsw.fitnesssystem.auth.authentication.application.dto.port;

import lombok.Builder;
import lombok.Data;

/**
 * 认证数据 DTO（由 auth 模块定义，作为端口契约的数据载体）
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 14:14
 * @since 1.0
 */
@Data
@Builder
public class AuthUserData {

    private Long userId;

    private Long campusId;

    private String username;

    /** BCrypt 哈希值 */
    private String password;

    /** 0-管理员, 1-教师, 2-学生 */
    private Integer userType;

    /** 0-禁用, 1-启用 */
    private Integer status;

}
