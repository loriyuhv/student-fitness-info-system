package com.wsw.fitnesssystem.handle_excel.domain.enums;

import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Excel导入业务类型枚举
 * 用于区分不同导入业务：用户信息导入、体测成绩导入等
 * @author loriyuhv
 * @version 1.0 2026/8/21 19:24
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum ExcelBizTypeEnum {
    USER_IMPORT("USER_IMPORT", "用户信息导入");

    private final String code;
    private final String desc;

    /**
     * 根据code获取枚举
     * @param code 业务编码
     * @return 对应枚举
     */
    public static ExcelBizTypeEnum getByCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new BizException(ResultCode.PARAM_INVALID, "不支持导入类型 ==> " + code));
    }

    /**
     * 获取全部业务编码集合，用于 /excel/import/types 接口返回给前端
     * @return 全部bizType code列表
     */
    public static List<String> getAllCodeList() {
        return Arrays.stream(values())
                .map(ExcelBizTypeEnum::getCode)
                .collect(Collectors.toList());
    }

}
