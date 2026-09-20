package com.wsw.fitnesssystem.fitness.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * fitness 模块错误码（体测）。
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 18:36
 * @since 1.0
 */
public enum FitnessErrorCode implements ErrorCode {

    DATA_NOT_FOUND(404101, HttpStatus.NOT_FOUND, "体测数据不存在"),
    DATA_ALREADY_EXIST(409101, HttpStatus.CONFLICT, "体测数据已存在"),
    SCORE_CALCULATE_ERROR(422101, HttpStatus.UNPROCESSABLE_ENTITY, "成绩计算失败"),
    DATA_IMPORT_ERROR(422102, HttpStatus.UNPROCESSABLE_ENTITY, "体测数据导入失败"),
    DATA_EXPORT_ERROR(422103, HttpStatus.UNPROCESSABLE_ENTITY, "体测数据导出失败");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    FitnessErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override public int code()       { return code; }
    @Override public String message() { return message; }

    public HttpStatus httpStatus() { return httpStatus; }

}
