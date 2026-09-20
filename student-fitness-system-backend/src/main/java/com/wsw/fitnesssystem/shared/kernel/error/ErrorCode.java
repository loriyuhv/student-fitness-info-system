package com.wsw.fitnesssystem.shared.kernel.error;

/**
 * 错误码契约（共享内核）。
 *
 * <p><b>职责：</b>只表达业务错误语义，不携带任何传输层信息（HTTP、gRPC、MQ 等）。
 * 由 Application / Infrastructure / Interfaces 三层共同依赖，符合六边形依赖方向。</p>
 *
 * <p><b>设计原则：</b></p>
 * <ul>
 *   <li><b>最小契约：</b>只暴露 code 与 message，不携带 HTTP 状态、日志级别等横向关注点</li>
 *   <li><b>实现类应为枚举：</b>保证错误码唯一来源，禁止匿名实现</li>
 *   <li><b>领域层不引用：</b>领域层使用独立的 {@code DomainErrorCode}</li>
 * </ul>
 *
 * <p><b>当前阶段：</b>P1 阶段仅建立契约，{@code code()} 返回 {@code int}。
 * 后续阶段（P5）将迁移为 {@code String} 字符串码。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 17:22
 * @since 1.0
 */
public interface ErrorCode {

    /**
     * 业务错误码（前端识别）。
     *
     * @return 全局唯一的业务错误码
     */
    int code();

    /**
     * 默认提示信息（面向终端用户）。
     *
     * <p>不应包含技术细节（堆栈、SQL、类名等）。</p>
     *
     * @return 默认的、可直接展示的消息
     */
    String message();

}
