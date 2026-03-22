package com.wsw.fitnesssystem.auth.application.authorization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * 可以：
 * 写 Redis
 * 转 GrantedAuthority
 * 做审计
 * @author loriyuhv
 * @version 1.0 2026/1/16 13:47
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthorization implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    /** 角色编码集合 */
    private Set<String> roles;

    /** 权限编码集合 */
    private Set<String> permissions;
}