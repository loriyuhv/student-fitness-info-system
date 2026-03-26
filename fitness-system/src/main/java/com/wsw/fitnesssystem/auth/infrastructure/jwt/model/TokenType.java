package com.wsw.fitnesssystem.auth.infrastructure.jwt.model;

/**
 * <p>JWT 令牌类型，仅限基础设施层使用。</p>
 * <p>禁止在 domain / application 层引用!!!</p>
 * <p>用途：防止拿 RefreshToken 调接口，属于安全边界校验</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 17:37
 * @since 1.0
 */
public enum TokenType {
    ACCESS,
    REFRESH,
}
