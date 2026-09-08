package com.wsw.fitnesssystem.shared.exception;

import com.wsw.fitnesssystem.shared.response.ResultCode;

/**
 * 系统异常（兜底）。
 * <p>
 * <b>语义：系统内部发生不可预期的技术故障</b>
 * <p>
 * <b>适用场景：</b>
 * <ul>
 *   <li>数据库连接失败、SQL 执行异常</li>
 *   <li>Redis 连接超时、操作失败</li>
 *   <li>文件 IO 异常（读写失败）</li>
 *   <li>第三方接口调用超时、返回异常</li>
 *   <li>线程池拒绝、资源耗尽</li>
 * </ul>
 * <p>
 * <b>使用原则：</b>
 * <ul>
 *   <li>基础设施层（Infrastructure）捕获原始技术异常后，包装为 SystemException 抛出</li>
 *   <li>应用层（Application）无需显式声明，由全局异常处理器统一转换</li>
 *   <li>不应在领域层（Domain）主动抛出 SystemException（领域层不依赖技术细节）</li>
 * </ul>
 * <p>
 * <b>HTTP 映射：</b>全局异常处理器统一映射为 500 级别错误。
 * <p>
 * <b>与其他异常的区别：</b>
 * <ul>
 *   <li>{@link BizException}：业务逻辑故障（可预期、应由调用方处理）</li>
 *   <li>{@link SystemException}：技术系统故障（不可预期、应由运维/开发处理）</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 22:55
 * @since 1.0
 */
public class SystemException extends BaseException {

    public SystemException(ResultCode resultCode) {
        super(resultCode);
    }

    public SystemException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public SystemException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }

    public SystemException(ResultCode resultCode, String message, Throwable cause) {
        super(resultCode, message, cause);
    }

}
