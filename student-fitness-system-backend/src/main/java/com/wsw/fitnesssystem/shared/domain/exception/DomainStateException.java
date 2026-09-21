package com.wsw.fitnesssystem.shared.domain.exception;

/**
 * 领域状态异常。
 *
 * <p><b>语义：</b>当前聚合处于某种状态，不允许执行目标操作。
 * 例如：已启用的账号再次启用、已删除的账号再次删除。</p>
 *
 * <p><b>与 {@link DomainConflictException} 的区别：</b></p>
 * <ul>
 *   <li>{@code DomainConflictException}：违反唯一性约束或业务规则冲突</li>
 *   <li>{@code DomainStateException}：违反状态机约束（状态迁移非法）</li>
 * </ul>
 *
 * <p><b>异常由应用层翻译</b>为具体的错误码，通常为 409 或 400。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/22 07:07
 * @since 1.0
 */
public class DomainStateException extends DomainException {

    public DomainStateException(String message) {
        super(message);
    }

    public DomainStateException(String message, Throwable cause) {
        super(message, cause);
    }

}
