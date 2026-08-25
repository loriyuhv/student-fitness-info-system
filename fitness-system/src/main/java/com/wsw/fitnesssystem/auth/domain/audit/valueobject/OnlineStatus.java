package com.wsw.fitnesssystem.auth.domain.audit.valueobject;

/**
 * 用户在线状态
 *
 * @author loriyuhv
 * @version 1.0 2026/8/25 11:34
 * @since 1.0
 */
public enum OnlineStatus {
    /** 在线（登录成功且未登出） */
    ONLINE,
    /** 已下线 */
    OFFLINE,
    /** 从未在线（登录失败） */
    NEVER_ONLINE
}
