package com.wsw.fitnesssystem.shared.domain.exception;

/**
 * 领域异常基类。
 * <p>表示领域规则或不变量被违反。不依赖任何框架、HTTP 语义。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/18 21:04
 * @since 1.0
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }

}
