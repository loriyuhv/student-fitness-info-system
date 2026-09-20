package com.wsw.fitnesssystem.shared.interfaces.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wsw.fitnesssystem.shared.kernel.error.CommonErrorCode;
import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应对象。
 *
 * <p><b>设计目标：</b></p>
 * <ul>
 *   <li>统一前后端交互协议，所有接口返回相同结构</li>
 *   <li>区分 HTTP 状态码（传输层）与业务状态码（应用层）</li>
 *   <li>包含时间戳，便于问题追踪与性能分析</li>
 * </ul>
 *
 * <p><b>字段说明：</b></p>
 * <ul>
 *   <li>{@code httpCode}：HTTP 状态码（200/400/401/403/500），用于网关/浏览器</li>
 *   <li>{@code bizCode}：业务状态码（来自 {@link ErrorCode}），供前端识别具体错误类型</li>
 *   <li>{@code message}：面向用户的提示信息</li>
 *   <li>{@code data}：业务数据（成功时返回）</li>
 *   <li>{@code timestamp}：响应生成时间（毫秒时间戳）</li>
 * </ul>
 *
 * <p><b>当前阶段：</b>P3 阶段 {@code bizCode} 仍为 int（数字码）。P5 阶段迁移为 String。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 18:10
 * @since 1.0
 */
@Data
@NoArgsConstructor
public class ApiResponse<T> {

    /**
     * HTTP 状态码
     */
    @JsonProperty("http_code")
    private Integer httpCode;

    /**
     * 业务状态码（前端识别）
     */
    @JsonProperty("biz_code")
    private String bizCode;

    /**
     * 提示信息
     */
    @JsonProperty("message")
    private String message;

    /**
     * 响应数据
     */
    @JsonProperty("data")
    private T data;

    /**
     * 时间戳
     */
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * 私有全参构造，强制使用静态工厂方法构建实例
     *
     * @param httpCode  HTTP 状态码
     * @param bizCode   业务状态码
     * @param message   提示信息
     * @param data      响应数据
     * @param timestamp 时间戳
     */
    private ApiResponse(Integer httpCode, String bizCode, String message, T data, Long timestamp) {
        this.httpCode = httpCode;
        this.bizCode = bizCode;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    /* ================= 成功响应 ================= */

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
            CommonErrorCode.SUCCESS.httpStatus().value(),
            CommonErrorCode.SUCCESS.code(),
            CommonErrorCode.SUCCESS.message(),
            data,
            System.currentTimeMillis()
        );
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
            CommonErrorCode.SUCCESS.httpStatus().value(),
            CommonErrorCode.SUCCESS.code(),
            message,
            data,
            System.currentTimeMillis()
        );
    }

    /* ================= 失败响应 ================= */

    /**
     * 构造失败响应。
     *
     * @param httpCode HTTP 状态码（由 {@code HttpStatusResolver} 解析）
     * @param ec       错误码契约
     */
    public static <T> ApiResponse<T> error(int httpCode, ErrorCode ec) {
        return error(httpCode, ec, ec.message());
    }

    public static <T> ApiResponse<T> error(int httpCode, ErrorCode ec, String message) {
        return new ApiResponse<>(
            httpCode,
            ec.code(),
            message,
            null,
            System.currentTimeMillis()
        );
    }

}
