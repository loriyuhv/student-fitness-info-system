package com.wsw.fitnesssystem.shared.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 接口统一返回对象
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 18:10
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {
    /** HTTP 状态码 */
    private Integer httpCode;

    /** 业务状态码（前端识别） */
    private Integer bizCode;

    /** 提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 时间戳 */
    private Long timestamp;

    /* ================= 成功响应 ================= */

    public static <T> ApiResult<T> success() {
        return success(null);
    }

    public static <T> ApiResult<T> success(T data) {
        return from(ResultCode.SUCCESS, data);
    }

    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(
            ResultCode.SUCCESS.httpCode(),
            ResultCode.SUCCESS.getCode(),
            message,
            data,
            System.currentTimeMillis()
        );
    }

    /* ================= 失败响应 ================= */

    public static <T> ApiResult<T> error(ResultCode resultCode) {
        return from(resultCode, null);
    }

    /* ================= 核心工厂方法 ================= */

    public static <T> ApiResult<T> from(ResultCode resultCode, T data) {
        return new ApiResult<>(
            resultCode.httpCode(),
            resultCode.getCode(),
            resultCode.getMessage(),
            data,
            System.currentTimeMillis()
        );
    }
}
