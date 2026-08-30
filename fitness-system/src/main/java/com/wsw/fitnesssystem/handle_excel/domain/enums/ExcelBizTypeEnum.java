package com.wsw.fitnesssystem.handle_excel.domain.enums;

import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * <p>Excel导入业务类型枚举</p>
 * <p>用于区分不同导入业务：用户信息导入、体测记录导入等</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 19:24
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum ExcelBizTypeEnum {

    USER_IMPORT("USER_IMPORT", "用户信息导入"),
    FITNESS_RECORD_IMPORT("FITNESS_RECORD_IMPORT", "体测记录导入");

    private final String code;
    private final String desc;

    /**
     * 根据code获取枚举
     * @param code 业务编码
     * @return 对应枚举
     */
    public static ExcelBizTypeEnum getByCode(String code) {
        return Arrays
            .stream(values())
            .filter(item -> item.getCode().equals(code))
            .findFirst()
            .orElseThrow(
                () -> new BizException(ResultCode.PARAM_INVALID, "Unsupported import type: " + code)
            );
    }

}
