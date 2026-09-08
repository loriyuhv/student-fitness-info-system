package com.wsw.fitnesssystem.handle_excel.application.port.output;

import com.wsw.fitnesssystem.shared.exception.BizException;

/**
 * 限流端口（输出端口）。
 * <p>
 * <b>职责：</b>防止单用户高频提交导入任务，保护系统资源。
 * <p>
 * <b>调用方向：</b>
 * <pre>ImportSubmissionService（应用层） → RateLimiterPort（契约） → RedisRateLimiterAdapter（基础设施）</pre>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/23 20:49
 * @since 1.0
 */
public interface RateLimiterPort {

    /**
     * 检查用户是否超过导入频率限制。
     * <p>
     * 若未超过限制则正常返回；若超过限制则抛出 {@link BizException}。
     *
     * @param userId 用户 ID（为 {@code null} 或 {@code <= 0} 时不做限制）
     * @throws BizException 超过限制时抛出
     */
    void checkRateLimit(Long userId);

}
