package com.wsw.fitnesssystem.user.domain.vb;

import com.wsw.fitnesssystem.shared.domain.exception.DomainValidationException;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户状态（业务可见性）
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 12:26
 * @since 1.0
 */
@Getter
public enum Status {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final Integer code;
    private final String desc;

    Status(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private static final Map<Integer, Status> CODE_MAP = Arrays.stream(values())
        .collect(Collectors.toMap(Status::getCode, Function.identity()));

    public static Status of(Integer code) {
        if (code == null) throw new DomainValidationException("用户状态不能为空");
        Status status = CODE_MAP.get(code);
        if (status == null) {
            throw new DomainValidationException("无效的用户状态编码：" + code);
        }
        return status;
    }

}
