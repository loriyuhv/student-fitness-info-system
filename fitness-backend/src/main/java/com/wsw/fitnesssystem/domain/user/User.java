package com.wsw.fitnesssystem.domain.user;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:23
 * @since 1.0
 */
public class User {
    private Long userId;
    private String username;
    private String password;
    private Integer status;

    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
