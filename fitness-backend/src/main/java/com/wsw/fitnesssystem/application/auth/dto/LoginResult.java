package com.wsw.fitnesssystem.application.auth.dto;

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
public class LoginResult {
    private String accessToken;
    private String refreshToken;
    private String tokenId;
    private long expiresIn;
}
