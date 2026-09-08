package com.wsw.fitnesssystem.user.application.dto.port;

import lombok.Builder;

import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 00:25
 * @since 1.0
 */
@Builder
public record UserAuthorizationInfo(Set<String> roles, Set<String> permissions) {
}
