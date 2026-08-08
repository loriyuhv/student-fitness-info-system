package com.wsw.fitnesssystem.auth.application.authentication.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 刷新Token返回结果
 *
 * @author loriyuhv
 * @version 1.0 2026/8/7 15:33
 * @since 1.0
 */
@Data
@Builder
public class RefreshTokenResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}
