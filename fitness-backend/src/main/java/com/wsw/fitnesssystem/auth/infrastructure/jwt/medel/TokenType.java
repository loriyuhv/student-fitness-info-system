package com.wsw.fitnesssystem.auth.infrastructure.jwt.medel;

/**
 * JWT 令牌类型，仅限基础设施层使用。
 * 禁止在 domain / application 层引用!!!
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 17:37
 * @since 1.0
 */
public enum TokenType {
    ACCESS,
    REFRESH,
}
