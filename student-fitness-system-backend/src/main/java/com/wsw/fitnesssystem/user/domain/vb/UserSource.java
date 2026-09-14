package com.wsw.fitnesssystem.user.domain.vb;

import lombok.Getter;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/26 12:28
 * @since 1.0
 */
@Getter
public enum UserSource {

    IMPORT(0, "导入"),
    SYNC(1, "同步"),
    MANUAL(2, "手动添加");

    private final int code;
    private final String desc;

    UserSource(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserSource of(int code) {
        for (UserSource source : values()) {
            if (source.code == code) return source;
        }
        throw new IllegalArgumentException("无效的用户来源编码：" + code);
    }

}
