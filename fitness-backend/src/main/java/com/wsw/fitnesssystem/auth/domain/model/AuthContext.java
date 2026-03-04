package com.wsw.fitnesssystem.auth.domain.model;

/**
 * 认证上下文
 * 表示一次「认证成功」的业务结果
 * 注意：不等同于 SpringSecurity 的 SecurityContext
 *
 * @author loriyuhv
 * @version 1.0 2026/1/19 14:22
 * @since 1.0
 */
public record AuthContext(Long userId) {}
