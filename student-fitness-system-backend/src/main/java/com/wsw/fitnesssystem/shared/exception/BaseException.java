package com.wsw.fitnesssystem.shared.exception;

import lombok.Getter;
import com.wsw.fitnesssystem.shared.response.ResultCode;

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
 *   <li>子类必须通过构造方法传入 {@link ResultCode}，确保错误码与消息统一</li>
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

    /** 统一错误码与消息 */
    protected final ResultCode resultCode;

    protected BaseException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    protected BaseException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    protected BaseException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.resultCode = resultCode;
    }

    protected BaseException(ResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.resultCode = resultCode;
    }

}
