package com.wsw.fitnesssystem.auth.application.service;

import com.wsw.fitnesssystem.auth.application.dto.TokenPair;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/22 16:48
 * @since 1.0
 */
public interface TokenService {
    TokenPair generate(
        Long userId,
        Long campusId,
        String username,
        String deviceId,
        Long tokenVersion,
        String accessTokenId,
        String refreshTokenId);
}
