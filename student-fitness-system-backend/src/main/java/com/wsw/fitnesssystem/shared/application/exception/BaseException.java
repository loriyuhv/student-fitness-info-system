package com.wsw.fitnesssystem.shared.application.exception;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;
import lombok.Getter;

/**
 * 应用层异常基类。
 *
 * <p><b>职责：</b>所有应用层自定义异常的父类，统一承载「错误语义」。</p>
 *
 * <p><b>设计原则：</b></p>
 * <ul>
 *   <li><b>依赖倒置：</b>持有 {@link ErrorCode} 契约而非具体枚举，
 *       Application 层不依赖任何模块的 {@code XxxErrorCode}</li>
 *   <li><b>传输无关：</b>不感知 HTTP 状态、日志级别等横向关注点，
 *       这些由接口层 {@code HttpStatusResolver} 独立处理</li>
 *   <li><b>不可实例化：</b>抽象类，仅作为 {@link BizException} /
 *       {@link SystemException} 的共同父类</li>
 *   <li><b>非受检：</b>继承 {@link RuntimeException}，不强制调用方 try-catch，
 *       与 Spring {@code @Transactional} 回滚策略兼容</li>
 * </ul>
 *
 * <p><b>构造器约定：</b></p>
 * <ul>
 *   <li>{@code super(errorCode.message())}：让 {@link #getMessage()} 返回默认文案，
 *       日志、调试友好</li>
 *   <li>{@code this.errorCode = errorCode}：保留错误码对象，
 *       接口层可读取完整语义</li>
 * </ul>
 *
 * <p><b>子类清单：</b></p>
 * <ul>
 *   <li>{@link BizException}：业务规则不满足（可预期，4xx）</li>
 *   <li>{@link SystemException}：技术系统故障（不可预期，5xx）</li>
 * </ul>
 *
 * <p><b>禁止事项：</b></p>
 * <ul>
 *   <li>领域层不得抛出本类或其子类（领域层抛 {@code DomainException}）</li>
 *   <li>Controller 层不得 try-catch 后吞掉异常（应让 {@code GlobalExceptionHandler} 处理）</li>
 *   <li>禁止匿名实现 {@link ErrorCode} 传给本类</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 22:55
 * @since 1.0
 */
@Getter
public abstract class BaseException extends RuntimeException {

    /** 统一错误码与消息（契约类型，不依赖具体枚举） */
    protected final ErrorCode errorCode;

    /**
     * 使用错误码的默认消息构造异常。
     *
     * @param errorCode 错误码（不可为 null）
     */
    protected BaseException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    /**
     * 使用自定义消息构造异常（覆盖错误码的默认文案）。
     *
     * @param errorCode 错误码
     * @param message   自定义消息（可拼接业务上下文）
     */
    protected BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 包装底层异常，保留原始堆栈。
     *
     * @param errorCode 错误码
     * @param cause     原始异常（如 Redis 超时、SQL 异常）
     */
    protected BaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.message(), cause);
        this.errorCode = errorCode;
    }

    /**
     * 全参构造：自定义消息 + 包装底层异常。
     *
     * @param errorCode 错误码
     * @param message   自定义消息
     * @param cause     原始异常
     */
    protected BaseException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}
