package com.wsw.fitnesssystem.user.domain.vb;

import com.wsw.fitnesssystem.shared.domain.exception.DomainValidationException;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private final Integer code;
    private final String desc;

    Gender(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private static final Map<Integer, Gender> CODE_MAP = Arrays.stream(values())
        .collect(Collectors.toMap(Gender::getCode, Function.identity()));

    public static Gender of(Integer code) {
        if (code == null) {
            throw new DomainValidationException("性别不能为空");
        }
        Gender gender = CODE_MAP.get(code);
        if (gender == null) {
            throw new DomainValidationException("无效的性别编码：" + code);
        }
        return gender;
    }

}
