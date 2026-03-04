package com.wsw.fitnesssystem.identity.infrastructure.session.support;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/16 14:33
 * @since 1.0
 */
public class LoginRedisKeys {
    private LoginRedisKeys() {}

    public static String onlineKey(Long userId) {
        return "login:online:" + userId;
    }
}
