package com.wsw.fitnesssystem.shared.domain.exception;

/**
 * 领域校验异常。
 * <p>表示用户输入或调用方传参违反了领域规则（如手机号格式错误、昵称超长）。</p>
 * <p>应用层翻译为 400 / PARAM_INVALID。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/18 21:05
 * @since 1.0
 */
public class DomainValidationException extends DomainException {

    public DomainValidationException(String message) {
        super(message);
    }

    public DomainValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
