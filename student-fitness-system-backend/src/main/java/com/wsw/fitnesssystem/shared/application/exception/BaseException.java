package com.wsw.fitnesssystem.shared.application.exception;

import com.wsw.fitnesssystem.shared.interfaces.web.response.ErrorCode;
import lombok.Getter;

/**
 * 异常基类。
 * <p>
 * <b>设计目标：</b>
 * <ul>
 *   <li>所有自定义异常的父类，统一承载“错误语义”</li>
 *   <li>与 HTTP 协议、Controller、接口协议完全解耦</li>
 *   <li>只关心业务语义，不关心传输方式</li>
 * </ul>
 * <p>
 * <b>使用规则：</b>
 * <ul>
 *   <li>子类必须通过构造方法传入 {@link ErrorCode}，确保错误码与消息统一</li>
 *   <li>不直接实例化，仅作为抽象基类</li>
 *   <li>继承自 {@link RuntimeException}，不强制调用方 try-catch</li>
 * </ul>
 *
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 22:55
 * @since 1.0
 */
@Getter
public abstract class BaseException extends RuntimeException {

    /** 统一错误码与消息（契约类型，不依赖具体枚举） */
    protected final ErrorCode errorCode;

    protected BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    protected BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    protected BaseException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}
