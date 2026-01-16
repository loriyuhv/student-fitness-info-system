package com.wsw.fitnesssystem.domain.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/15 14:38
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class AuthUser {
    private Long userId;
    private String username;
    private String password;
    private Integer status;
    private Set<String> permissions;

    public boolean isEnabled() {
        return status != null && status == 1;
    }

    public Set<String> allAuthorities() {
        return new HashSet<>(permissions);
    }
}
