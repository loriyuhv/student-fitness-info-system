package com.wsw.fitnesssystem.shared.application.exception;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;

/**
 * 业务异常。
 * <p>
 * <b>语义：请求合法，但业务规则不满足</b>
 * <p>
 * <b>适用场景：</b>
 * <ul>
 *   <li>参数校验失败（如：手机号格式错误、密码长度不足）</li>
 *   <li>业务状态不合法（如：用户已存在）</li>
 *   <li>权限不足（如：无操作权限、数据权限校验失败）</li>
 *   <li>资源不存在（如：用户不存在、文件未找到）</li>
 * </ul>
 * <p>
 * <b>HTTP 映射：</b>全局异常处理器统一映射为 400 级别错误。
 * <p>
 * <b>与其他异常的区别：</b>
 * <ul>
 *   <li>{@link SystemException}：技术系统故障（DB、IO、第三方），不可预期</li>
 *   <li>{@link BizException}：业务逻辑故障（用户输入错误、状态不匹配），可预期</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 22:55
 * @since 1.0
 */
public class BizException extends BaseException {

    public BizException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BizException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BizException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public BizException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}
