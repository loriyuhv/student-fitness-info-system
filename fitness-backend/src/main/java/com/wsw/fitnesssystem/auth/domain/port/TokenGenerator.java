package com.wsw.fitnesssystem.auth.domain.port;

import com.wsw.fitnesssystem.auth.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.domain.model.TokenPair;

/**
 * 双Token生成
 * @author loriyuhv
 * @version 1.0 2026/3/21 10:04
 * @since 1.0
 */
public interface TokenGenerator {
    TokenPair generate(AuthUser user, String accessTokenId, String refreshTokenId);
}
