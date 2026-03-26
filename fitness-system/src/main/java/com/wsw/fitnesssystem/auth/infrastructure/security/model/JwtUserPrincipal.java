package com.wsw.fitnesssystem.auth.infrastructure.security.model;

import java.io.Serializable;

/**
 * JWT 用户主体信息（放入 SecurityContext principal）
 *
 * @author loriyuhv
 * @version 1.0 2026/3/21 15:14
 * @since 1.0
 */
public record JwtUserPrincipal(
    String username,
    String accessTokenId,
    Long campusId,
    Long userId
) implements Serializable {}
