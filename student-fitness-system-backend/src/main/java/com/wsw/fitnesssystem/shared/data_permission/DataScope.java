package com.wsw.fitnesssystem.shared.data_permission;

import lombok.Getter;

/**
 * 数据权限范围。
 * <p>与 {@code sys_role.data_scope} 字段一一对应。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/10 11:55
 * @since 1.0
 */
@Getter
public enum DataScope {

    ALL(0, "全部数据"),
    SELF(1, "仅本人"),
    CLASS(2, "本班"),
    COLLEGE(3, "本学院"),
    CUSTOM(4, "自定义");

    private final int code;
    private final String desc;

    DataScope(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DataScope of(Integer code) {
        if (code == null) throw new IllegalArgumentException("数据权限编码不能为空");

        for (DataScope s : values()) {
            if (s.code == code) return s;
        }

        throw new IllegalArgumentException("无效的数据权限编码：" + code);
    }

}
