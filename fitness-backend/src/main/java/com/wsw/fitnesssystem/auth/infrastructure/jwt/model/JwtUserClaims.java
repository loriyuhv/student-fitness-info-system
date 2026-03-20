package com.wsw.fitnesssystem.auth.infrastructure.jwt.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/5 20:10
 * @since 1.0
 */
@Data
@Builder
public class JwtUserClaims {
    private Long userId;

    private Long campusId;

    private String username;

    private String tokenId;
}
