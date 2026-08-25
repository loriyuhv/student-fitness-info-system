package com.wsw.fitnesssystem.auth.authentication.interfaces.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 * <p>设备类型：PC、PHONE</p>
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

    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

}
