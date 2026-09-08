package com.wsw.fitnesssystem.shared.response;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应对象。
 * <p>
 * <b>设计目标：</b>
 * <ul>
 *   <li>统一前后端交互协议，所有接口返回相同结构</li>
 *   <li>区分 HTTP 状态码（传输层）与业务状态码（应用层）</li>
 *   <li>包含时间戳，便于问题追踪与性能分析</li>
 * </ul>
 * <p>
 * <b>字段说明：</b>
 * <ul>
 *   <li>{@code httpCode}：HTTP 状态码（200/400/401/403/500），用于网关/浏览器</li>
 *   <li>{@code bizCode}：业务状态码（来自 {@link ResultCode}），供前端识别具体错误类型</li>
 *   <li>{@code message}：面向用户的提示信息</li>
 *   <li>{@code data}：业务数据（成功时返回）</li>
 *   <li>{@code timestamp}：响应生成时间（毫秒时间戳）</li>
 * </ul>
 * <p>
 * <b>使用方式：</b>
 * <ul>
 *   <li>成功响应：使用 {@link #success()} 或 {@link #success(Object)}</li>
 *   <li>失败响应：使用 {@link #error(ResultCode)} 或 {@link #error(ResultCode, String)}</li>
 *   <li>不建议直接实例化，统一使用静态工厂方法</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 18:10
 * @since 1.0
 */
@Data
@NoArgsConstructor
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

    /**
     * 私有全参构造，强制使用静态工厂方法构建实例
     * @param httpCode HTTP 状态码
     * @param bizCode 业务状态码
     * @param message 提示信息
     * @param data 响应数据
     * @param timestamp 时间戳
     */
    private ApiResult(Integer httpCode, Integer bizCode, String message, T data, Long timestamp) {
        this.httpCode = httpCode;
        this.bizCode = bizCode;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    /* ================= 成功响应 ================= */

    public static <T> ApiResult<T> success() {
        return success(ResultCode.SUCCESS);
    }

    public static <T> ApiResult<T> success(T data) {
        return from(ResultCode.SUCCESS, data);
    }

    public static <T> ApiResult<T> success(ResultCode resultCode) {
        return from(resultCode, null);
    }


    public static <T> ApiResult<T> success(ResultCode resultCode, String message) {
        return from(resultCode, null, message);
    }

    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(
            ResultCode.SUCCESS.httpCode(),
            ResultCode.SUCCESS.getCode(),
            message, data,
            System.currentTimeMillis()
        );
    }

    /* ================= 失败响应 ================= */

    public static <T> ApiResult<T> error(ResultCode resultCode) {
        return from(resultCode, null);
    }

    public static <T> ApiResult<T> error(ResultCode resultCode, String message) {
        return from(resultCode, null, message);
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

    public static <T> ApiResult<T> from(ResultCode resultCode, T data, String message) {
        return new ApiResult<>(
                resultCode.httpCode(),
                resultCode.getCode(),
                message,
                data,
                System.currentTimeMillis()
        );
    }

}
