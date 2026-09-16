package com.wsw.fitnesssystem.iam.authentication.domain.vb;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/26 12:29
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum UserType {

    ADMIN(0, "管理员"),
    TEACHER(1, "教师"),
    STUDENT(2, "学生");

    private final int code;
    private final String desc;

    public static UserType of(int code) {
        for (UserType type : values()) {
            if (type.code == code) return type;
        }
        throw new IllegalArgumentException("无效的用户类型编码：" + code);
    }

}
