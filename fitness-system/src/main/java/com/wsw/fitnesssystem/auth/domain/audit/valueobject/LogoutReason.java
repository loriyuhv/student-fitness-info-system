package com.wsw.fitnesssystem.auth.domain.audit.valueobject;

/**
 * 登出原因
 *
 * @author loriyuhv
 * @version 1.0 2026/8/25 11:36
 * @since 1.0
 */
public enum LogoutReason {
    /** 主动退出 */
    LOGOUT,
    /** 被踢下线 */
    KICK,
    /** Token 过期 */
    EXPIRE
}
