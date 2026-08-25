package com.wsw.fitnesssystem.auth.domain.audit.valueobject;

/**
 * 登录失败上下文（仅失败时存在）
 *
 * @author loriyuhv
 * @version 1.0 2026/8/25 11:37
 * @since 1.0
 */
public record FailureContext(String failReason) {
}
