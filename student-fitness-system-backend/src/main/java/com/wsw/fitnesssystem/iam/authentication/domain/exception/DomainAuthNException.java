package com.wsw.fitnesssystem.iam.authentication.domain.exception;

import com.wsw.fitnesssystem.shared.domain.exception.DomainException;

/**
 * 领域认证异常（iam.authentication 专属）。
 *
 * <p><b>语义：</b>iam 认证领域内的凭证校验失败，如密码不匹配。
 * 属于「认证行为失败」，不是输入校验失败，也不是状态非法。</p>
 *
 * <p><b>为什么放 iam 而不是 shared？</b></p>
 * <ul>
 *   <li>「认证失败」语义<b>只属于 iam</b>，其他模块不会抛同类异常</li>
 *   <li>放 shared 会造成「过度共享」，混淆通用语义与专属语义</li>
 *   <li>若 iam 未来独立成模块，本异常随 iam 一起迁移，语义自洽</li>
 * </ul>
 *
 * <p><b>安全提示：</b>本异常的 message 包含具体失败原因（如「密码不匹配」），
 * 供审计与排查使用。<b>不应对终端用户直接展示</b>——认证场景应统一返回
 * 「账号或密码错误」以防账号枚举攻击。</p>
 *
 * <p><b>异常翻译：</b>由应用层（{@code LoginCommandService}）捕获后，统一翻译为
 * {@code BizException(IamAuthNErrorCode.USER_LOGIN_ERROR)}。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/22 07:08
 * @since 1.0
 */
public class DomainAuthNException extends DomainException {

    public DomainAuthNException(String message) {
        super(message);
    }

    public DomainAuthNException(String message, Throwable cause) {
        super(message, cause);
    }

}
