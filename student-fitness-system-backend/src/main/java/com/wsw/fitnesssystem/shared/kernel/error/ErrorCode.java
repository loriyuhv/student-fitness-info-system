package com.wsw.fitnesssystem.shared.kernel.error;

/**
 * 错误码契约（共享内核）。
 *
 * <p><b>职责：</b>只表达业务错误语义，不携带任何传输层信息（HTTP、gRPC、MQ 等）。
 * 由 Application / Infrastructure / Interfaces 三层共同依赖，符合六边形架构的依赖方向。</p>
 *
 * <p><b>设计原则：</b></p>
 * <ul>
 *   <li><b>最小契约：</b>只暴露 {@code code()} 与 {@code message()}，
 *       不携带 HTTP 状态、日志级别等横向关注点</li>
 *   <li><b>实现类必须为枚举：</b>保证错误码唯一来源，禁止匿名实现</li>
 *   <li><b>传输无关：</b>HTTP 状态由接口层 {@code HttpStatusResolver} 独立维护，
 *       未来接入 gRPC / MQ / CLI 时业务代码零改动</li>
 *   <li><b>领域层不引用：</b>领域层使用独立的 {@link DomainErrorCode}，
 *       由接口层 {@code DomainErrorTranslator} 翻译为 {@code ErrorCode}</li>
 * </ul>
 *
 * <p><b>命名规范：</b>{@code {域}.{子域}.{错误}}，全小写，多词用 {@code _} 连接。</p>
 * <ul>
 *   <li>{@code common.param.invalid} —— 跨上下文通用的错误码</li>
 *   <li>{@code iam.token.expired} —— iam 上下文专属错误码</li>
 *   <li>{@code user.phone.already_exists} —— user 上下文专属错误码</li>
 * </ul>
 *
 * <p><b>归属判断：</b></p>
 * <ul>
 *   <li>跨上下文通用 → {@link CommonErrorCode}（本包）</li>
 *   <li>模块专属 → {@code {module}.error.XxxErrorCode}</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 17:22
 * @since 1.0
 */
public interface ErrorCode {

    /**
     * 业务错误码（前端识别）。
     *
     * <p>全局唯一，前端依赖此值做精确的业务分支判断（如 Token 刷新、权限跳转）。</p>
     *
     * @return 全局唯一的字符串错误码，形如 {@code "iam.token.expired"}
     */
    String code();

    /**
     * 默认提示信息（面向终端用户）。
     *
     * <p>不应包含技术细节（堆栈、SQL、类名）或业务参数（手机号、ID）。</p>
     *
     * @return 默认的、可直接展示的消息
     */
    String message();

}
