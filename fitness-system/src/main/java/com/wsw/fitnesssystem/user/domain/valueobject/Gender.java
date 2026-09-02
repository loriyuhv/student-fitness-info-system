package com.wsw.fitnesssystem.user.domain.valueobject;

import lombok.Getter;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 09:44
 * @since 1.0
 */
@Getter
public enum Gender {

    UNKNOWN(0, "未知"),
    MALE(1, "男"),
    FEMALE(2, "女");

    private final int code;
    private final String desc;

    Gender(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static Gender of(int code) {
        for (Gender gender : values()) {
            if (gender.code == code) {
                return gender;
            }
        }
        return UNKNOWN; // 默认未知
    }

}
