package com.wsw.fitnesssystem.auth.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 10:09
 * @since 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenPair {
    /** 访问令牌ID */
    private String accessTokenId;

    /** 刷新令牌ID */
    private String refreshTokenId;

    /** 访问令牌 */
    private String accessToken;

    /** 刷新令牌 */
    private String refreshToken;

    /** 访问令牌过期时间（秒） */
    private long accessTokenExpiresIn;

    /** 刷新令牌过期时间（秒） */
    private long refreshTokenExpiresIn;
}
