package com.wsw.fitnesssystem.auth.application.authorization.dto;

import java.util.Set;

/**
 * 可以：
 * 写 Redis
 * 转 GrantedAuthority
 * 做审计
 *
 * @param roles       角色编码集合
 * @param permissions 权限编码集合
 * @author loriyuhv
 * @version 1.0 2026/1/16 13:47
 * @since 1.0
 */
public record UserAuthorization(
    Long userId,
    Set<String> roles,
    Set<String> permissions
) {}