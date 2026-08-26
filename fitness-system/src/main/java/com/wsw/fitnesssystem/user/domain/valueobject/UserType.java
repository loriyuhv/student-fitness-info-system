package com.wsw.fitnesssystem.user.domain.valueobject;

import lombok.Getter;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/26 12:29
 * @since 1.0
 */
@Getter
public enum UserType {

    ADMIN(0, "管理员"),
    TEACHER(1, "教师"),
    STUDENT(2, "学生");

    private final int code;
    private final String desc;

    UserType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserType of(int code) {
        for (UserType type : values()) {
            if (type.code == code) return type;
        }
        throw new IllegalArgumentException("Unknown user type: " + code);
    }

}
