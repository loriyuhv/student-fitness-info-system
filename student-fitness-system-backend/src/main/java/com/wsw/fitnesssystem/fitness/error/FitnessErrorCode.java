package com.wsw.fitnesssystem.fitness.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;

/**
 * fitness 模块错误码（体测）。
 *
 * <p><b>职责：</b>承载体测记录、成绩计算、体测数据导入导出相关的错误码。</p>
 *
 * <p><b>覆盖范围：</b></p>
 * <ul>
 *   <li><b>数据状态：</b>体测数据不存在、体测数据已存在</li>
 *   <li><b>业务处理：</b>成绩计算失败、数据导入失败、数据导出失败</li>
 * </ul>
 *
 * <p><b>命名规范：</b>{@code fitness.*}</p>
 *
 * <p><b>HTTP 映射：</b>由接口层 {@code HttpStatusResolver} 显式注册，
 * 本枚举不感知 HTTP 语义。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 18:36
 * @since 1.0
 */
public enum FitnessErrorCode implements ErrorCode {

    /** 体测数据不存在 */
    DATA_NOT_FOUND("fitness.data.not_found", "体测数据不存在"),

    /** 体测数据已存在 */
    DATA_ALREADY_EXIST("fitness.data.already_exist", "体测数据已存在"),

    /** 成绩计算失败（BMI、肺活量等标准分转换异常） */
    SCORE_CALCULATE_ERROR("fitness.score.calculate_error", "成绩计算失败"),

    /** 体测数据导入失败 */
    DATA_IMPORT_ERROR("fitness.data.import_error", "体测数据导入失败"),

    /** 体测数据导出失败 */
    DATA_EXPORT_ERROR("fitness.data.export_error", "体测数据导出失败");

    private final String code;
    private final String message;

    FitnessErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

}
