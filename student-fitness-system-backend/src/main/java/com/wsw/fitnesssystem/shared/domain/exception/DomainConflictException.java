package com.wsw.fitnesssystem.shared.domain.exception;

/**
 * 领域冲突异常。
 * <p>表示领域不变量被违反（如手机号已占用、状态不允许操作）。</p>
 * <p>应用层翻译为 409 / CONFLICT。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/18 21:06
 * @since 1.0
 */
public class DomainConflictException extends DomainException {

    public DomainConflictException(String message) {
        super(message);
    }

    public DomainConflictException(String message, Throwable cause) {
        super(message, cause);
    }

}
