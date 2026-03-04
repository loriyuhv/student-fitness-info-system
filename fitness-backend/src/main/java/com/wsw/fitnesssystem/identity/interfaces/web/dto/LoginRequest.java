package com.wsw.fitnesssystem.identity.interfaces.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 *
 * @author loriyuhv
 * @version 1.0 2026/1/11 15:46
 * @since 1.0
 */
@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 设备类型
     * PC / MOBILE / PAD
     */
    @NotBlank(message = "设备类型不能为空")
    private String deviceType;
}
