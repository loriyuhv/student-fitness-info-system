package com.wsw.fitnesssystem.shared.domain.valueobject;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/9 01:13
 * @since 1.0
 */
public record Operator(
        Long campusId,
        Long userId,
        String username,
        Integer userType
) {
    public boolean isAdmin(){
        return userType == 0;
    }

    public boolean isTeacher(){
        return userType == 1;
    }

    public boolean isStudent(){
        return userType == 2;
    }
}
