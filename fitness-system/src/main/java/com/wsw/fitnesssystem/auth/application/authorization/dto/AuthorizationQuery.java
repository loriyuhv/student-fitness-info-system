package com.wsw.fitnesssystem.auth.application.authorization.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/8 18:06
 * @since 1.0
 */
@Data
@Builder
public class AuthorizationQuery {
    private Long userId;

    private Long campusId;
}
