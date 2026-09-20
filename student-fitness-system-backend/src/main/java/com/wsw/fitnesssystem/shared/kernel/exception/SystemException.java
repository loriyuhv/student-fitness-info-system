package com.wsw.fitnesssystem.shared.kernel.exception;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;

/**
 * 系统异常（技术故障兜底）。
 *
 * <p><b>语义：</b>系统内部发生不可预期的技术故障。调用方 / 用户无法修正，
 * 需要开发 / 运维介入排查。</p>
 *
 * <p><b>适用场景：</b></p>
 * <ul>
 *   <li><b>数据库故障：</b>连接失败、SQL 执行异常、死锁</li>
 *   <li><b>缓存故障：</b>Redis 连接超时、序列化失败</li>
 *   <li><b>文件 IO：</b>读写失败、磁盘满</li>
 *   <li><b>第三方接口：</b>调用超时、返回异常、限流</li>
 *   <li><b>资源耗尽：</b>线程池拒绝、OOM</li>
 * </ul>
 *
 * <p><b>抛出的位置：</b>基础设施层（Infrastructure）。捕获原始技术异常后
 * 包装为本异常，向上抛出。</p>
 *
 * <p><b>日志级别：</b>ERROR。需要开发 / 运维介入，建议接入告警。</p>
 *
 * <p><b>HTTP 映射：</b>由接口层 {@code HttpStatusResolver} 根据具体
 * {@link ErrorCode} 解析（通常为 5xx：500 / 502 / 503 / 504）。</p>
 *
 * <p><b>使用原则：</b></p>
 * <ul>
 *   <li>基础设施层捕获原始技术异常后，包装为 {@code SystemException} 抛出</li>
 *   <li>应用层无需显式声明，由全局异常处理器统一转换</li>
 *   <li><b>不应在领域层主动抛出</b>（领域层不依赖技术细节）</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>{@code
 * // Redis 连接失败
 * try {
 *     redisTemplate.opsForValue().set(key, value, ttl);
 * } catch (RedisConnectionException e) {
 *     throw new SystemException(
 *         CommonErrorCode.CACHE_ERROR,
 *         "Redis 写入失败，key=" + key,
 *         e
 *     );
 * }
 *
 * // 第三方接口超时
 * try {
 *     thirdPartyClient.call(request);
 * } catch (TimeoutException e) {
 *     throw new SystemException(CommonErrorCode.INTERFACE_TIMEOUT, e);
 * }
 * }</pre>
 *
 * <p><b>与 {@link BizException} 的区别：</b></p>
 * <ul>
 *   <li>{@link BizException}：业务逻辑失败，可预期，由调用方 / 用户处理</li>
 *   <li>{@code SystemException}：技术系统故障，不可预期，由开发 / 运维处理</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 22:55
 * @since 1.0
 */
public class SystemException extends BaseException {

    /**
     * 使用错误码的默认消息构造异常。
     *
     * @param errorCode 系统错误码（通常来自 {@link com.wsw.fitnesssystem.shared.kernel.error.CommonErrorCode}）
     */
    public SystemException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 使用自定义消息构造异常（拼接技术上下文，如 key、请求 ID）。
     *
     * @param errorCode 系统错误码
     * @param message   自定义消息
     */
    public SystemException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 包装底层技术异常，保留原始堆栈。
     *
     * <p><b>强烈推荐使用此构造器</b>——保留 cause 是排查问题的关键。</p>
     *
     * @param errorCode 系统错误码
     * @param cause     原始技术异常（如 RedisConnectionException、SQLException）
     */
    public SystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    /**
     * 全参构造：自定义消息 + 包装底层异常。
     *
     * @param errorCode 系统错误码
     * @param message   自定义消息
     * @param cause     原始技术异常
     */
    public SystemException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}
