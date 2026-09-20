package com.wsw.fitnesssystem.shared.application.exception;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;

/**
 * 业务异常。
 *
 * <p><b>语义：</b>请求合法，但业务规则不满足。属于可预期的失败，
 * 调用方 / 用户可通过修正输入或调整操作解决。</p>
 *
 * <p><b>适用场景：</b></p>
 * <ul>
 *   <li><b>参数校验失败：</b>手机号格式错误、密码长度不足</li>
 *   <li><b>业务状态不合法：</b>用户已存在、订单状态不允许取消</li>
 *   <li><b>权限不足：</b>无操作权限、数据权限校验失败</li>
 *   <li><b>资源不存在：</b>用户不存在、文件未找到</li>
 *   <li><b>唯一约束冲突：</b>手机号、邮箱、用户名已占用</li>
 * </ul>
 *
 * <p><b>抛出的位置：</b>应用层（Application Service）。</p>
 *
 * <p><b>日志级别：</b>WARN。属于可预期失败，不需要开发介入。</p>
 *
 * <p><b>HTTP 映射：</b>由接口层 {@code HttpStatusResolver} 根据具体
 * {@link ErrorCode} 解析（通常为 4xx：400 / 401 / 403 / 404 / 409 / 422）。</p>
 *
 * <p><b>使用示例：</b></p>
 * <pre>{@code
 * // 手机号重复
 * if (userRepo.existsByPhone(cmd.phone())) {
 *     throw new BizException(
 *         UserErrorCode.PHONE_ALREADY_EXISTS,
 *         "手机号 " + maskPhone(cmd.phone()) + " 已被注册"
 *     );
 * }
 *
 * // 权限不足
 * if (!operator.hasPermission(Permission.USER_DELETE)) {
 *     throw new BizException(IamAuthZErrorCode.PERMISSION_DENIED);
 * }
 * }</pre>
 *
 * <p><b>与 {@link SystemException} 的区别：</b></p>
 * <ul>
 *   <li>{@code BizException}：业务逻辑失败，可预期，由调用方 / 用户处理</li>
 *   <li>{@link SystemException}：技术系统故障，不可预期，由开发 / 运维处理</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 22:55
 * @since 1.0
 */
public class BizException extends BaseException {

    /**
     * 使用错误码的默认消息构造异常。
     *
     * @param errorCode 业务错误码
     */
    public BizException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 使用自定义消息构造异常（覆盖默认文案）。
     *
     * @param errorCode 业务错误码
     * @param message   自定义消息（可拼接业务上下文）
     */
    public BizException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 包装底层异常，保留原始堆栈。
     *
     * @param errorCode 业务错误码
     * @param cause     原始异常
     */
    public BizException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    /**
     * 全参构造：自定义消息 + 包装底层异常。
     *
     * @param errorCode 业务错误码
     * @param message   自定义消息
     * @param cause     原始异常
     */
    public BizException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}
