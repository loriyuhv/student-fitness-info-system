package com.wsw.fitnesssystem.shared.interfaces.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wsw.fitnesssystem.shared.kernel.error.CommonErrorCode;
import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 统一 API 响应对象（响应信封）。
 *
 * <p><b>核心职责：</b>承载所有 HTTP 接口的响应体，让前端只需一套解析逻辑，
 * 无论成功还是失败都遵循相同结构。</p>
 *
 * <p><b>字段说明：</b></p>
 * <ul>
 *   <li>{@code httpCode}：HTTP 状态码（200/400/401/403/500…），
 *       与响应行保持一致，冗余方便前端和日志直接读取</li>
 *   <li>{@code bizCode}：业务错误码（字符串，来自 {@link ErrorCode}），
 *       供前端做精确的业务分支判断（如 Token 刷新、权限跳转）</li>
 *   <li>{@code message}：面向用户的提示信息（默认来自错误码，可被覆盖）</li>
 *   <li>{@code data}：业务数据（成功时返回，失败时为 null）</li>
 *   <li>{@code timestamp}：响应生成时间（毫秒时间戳），便于问题追踪与性能分析</li>
 * </ul>
 *
 * <p><b>为什么用响应信封（Envelope）模式？</b></p>
 * <ul>
 *   <li><b>成功与失败结构统一：</b>前端只需一套解析逻辑，不需要为错误场景写特殊处理</li>
 *   <li><b>业务码与传输码解耦：</b>{@code httpCode} 表达传输层语义，
 *       {@code bizCode} 表达业务层语义，互不干扰</li>
 *   <li><b>可扩展：</b>未来若需增加字段（如 {@code traceId}），只需在本类加字段，
 *       所有接口自动生效</li>
 * </ul>
 *
 * <p><b>为什么 {@code httpCode} 字段与响应行冗余？</b></p>
 * <ul>
 *   <li><b>前端便利：</b>无需读 {@code response.status}，直接读 {@code body.httpCode}</li>
 *   <li><b>日志友好：</b>APM 抓取 body 时能直接看到状态码，不用额外解析响应行</li>
 *   <li><b>代价极小：</b>约 4 字节冗余，收益远大于成本</li>
 * </ul>
 *
 * <p><b>为什么 {@code bizCode} 用 String 而非 int？</b></p>
 * <ul>
 *   <li><b>自解释：</b>{@code "iam.token.expired"} 比 {@code 401102} 直观 10 倍</li>
 *   <li><b>与 HTTP 解耦：</b>不再受「数字前缀暗示 HTTP 状态」的隐性耦合</li>
 *   <li><b>跨服务对齐：</b>与其它服务对接时无需同步码表</li>
 * </ul>
 *
 * <p><b>为什么用私有构造 + 静态工厂？</b></p>
 * <ul>
 *   <li><b>语义清晰：</b>{@code success(data)} 比 {@code new ApiResponse<>(200, ...)} 易读</li>
 *   <li><b>封装细节：</b>{@code timestamp} 自动生成，调用方无需传</li>
 *   <li><b>可演进：</b>未来若需加埋点、日志等逻辑，只需改工厂方法，调用方零改动</li>
 * </ul>
 *
 * <p><b>序列化约定：</b></p>
 * <ul>
 *   <li>字段采用 <b>snake_case</b> 序列化：{@code http_code} / {@code biz_code} / {@code timestamp}</li>
 *   <li>由 {@link JsonProperty} 逐个字段显式声明，不使用全局 Jackson 命名策略，
 *       避免影响其它 DTO</li>
 *   <li><b>⚠️ 前端需同步：</b>TypeScript 接口定义应使用 {@code http_code} / {@code biz_code}，
 *       不是 {@code httpCode} / {@code bizCode}</li>
 * </ul>
 *
 * <p><b>典型使用场景：</b></p>
 * <ul>
 *   <li>Controller 成功返回：{@code return ApiResponse.success(data);}</li>
 *   <li>全局异常处理器：{@code ApiResponse.error(httpCode, errorCode, message);}</li>
 * </ul>
 *
 * @param <T> 业务数据类型
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 18:10
 * @since 1.0
 */
@Data
@NoArgsConstructor
public class ApiResponse<T> {

    /**
     * HTTP 状态码（与响应行保持一致）。
     *
     * <p>序列化字段名：{@code http_code}</p>
     */
    @JsonProperty("http_code")
    private Integer httpCode;

    /**
     * 业务错误码（字符串）。
     *
     * <p>序列化字段名：{@code biz_code}</p>
     * <p>由 {@link ErrorCode#code()} 提供，形如 {@code "iam.token.expired"}。</p>
     */
    @JsonProperty("biz_code")
    private String bizCode;

    /**
     * 提示信息（面向终端用户）。
     *
     * <p>序列化字段名：{@code message}</p>
     * <p>默认来自 {@link ErrorCode#message()}，可被异常自定义消息覆盖。</p>
     */
    @JsonProperty("message")
    private String message;

    /**
     * 业务数据（成功时返回）。
     *
     * <p>序列化字段名：{@code data}</p>
     * <p>失败时为 {@code null}。</p>
     */
    @JsonProperty("data")
    private T data;

    /**
     * 响应生成时间（毫秒时间戳）。
     *
     * <p>序列化字段名：{@code timestamp}</p>
     * <p>服务端统一生成，便于问题追踪与性能分析。</p>
     */
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * 私有全参构造：强制使用静态工厂方法构建实例。
     *
     * <p>私有化的目的：</p>
     * <ul>
     *   <li>防止调用方绕过工厂方法直接 new，破坏「{@code timestamp} 自动生成」的约定</li>
     *   <li>为未来增加埋点、日志等初始化逻辑留出空间</li>
     * </ul>
     *
     * @param httpCode  HTTP 状态码
     * @param bizCode   业务错误码
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

    /**
     * 成功响应（无数据）。
     *
     * <p>适用场景：删除操作、无返回值的写操作。</p>
     *
     * @param <T> 业务数据类型
     * @return 成功响应，{@code data} 为 {@code null}
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 成功响应（带数据，默认消息）。
     *
     * <p>最常用的成功工厂方法。</p>
     *
     * @param data 业务数据（可为 {@code null}）
     * @param <T>  业务数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
            HttpStatus.OK.value(),
            CommonErrorCode.SUCCESS.code(),
            CommonErrorCode.SUCCESS.message(),
            data,
            System.currentTimeMillis()
        );
    }

    /**
     * 成功响应（带数据 + 自定义消息）。
     *
     * <p>适用场景：需要提示用户具体成功信息（如「导入完成，共 100 条」）。</p>
     *
     * @param message 自定义消息（覆盖默认的「操作成功」）
     * @param data    业务数据
     * @param <T>     业务数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
            HttpStatus.OK.value(),
            CommonErrorCode.SUCCESS.code(),
            message,
            data,
            System.currentTimeMillis()
        );
    }

    /* ================= 失败响应 ================= */

    /**
     * 失败响应（默认消息）。
     *
     * <p>消息取 {@link ErrorCode#message()}。</p>
     *
     * @param httpCode HTTP 状态码（由 {@code HttpStatusResolver} 解析）
     * @param ec       错误码契约
     * @param <T>      业务数据类型
     * @return 失败响应，{@code data} 为 {@code null}
     */
    public static <T> ApiResponse<T> error(int httpCode, ErrorCode ec) {
        return error(httpCode, ec, ec.message());
    }

    /**
     * 失败响应（自定义消息）。
     *
     * <p>适用场景：需要拼接业务上下文（如「手机号 138****8000 已被注册」）。</p>
     *
     * @param httpCode HTTP 状态码
     * @param ec       错误码契约
     * @param message  自定义消息（覆盖默认消息）
     * @param <T>      业务数据类型
     * @return 失败响应
     */
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
