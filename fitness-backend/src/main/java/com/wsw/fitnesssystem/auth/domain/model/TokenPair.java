package com.wsw.fitnesssystem.auth.domain.model;

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
    private String accessTokenId;
    private String refreshTokenId;
    private String accessToken;
    private String refreshToken;
    private long expire;
}
