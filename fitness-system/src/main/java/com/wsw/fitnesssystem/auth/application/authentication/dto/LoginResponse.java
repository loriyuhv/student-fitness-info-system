package com.wsw.fitnesssystem.auth.application.authentication.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 登录返回结果
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:21
 * @since 1.0
 */
@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}
