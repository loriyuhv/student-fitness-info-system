package com.wsw.fitnesssystem.handle_excel.core.port;

/**
 * 导入频率限制端口
 * <p>防止单个用户高频提交导入任务，保护系统资源</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/23 20:49
 * @since 1.0
 */
public interface ImportRateLimitPort {

    /**
     * 检查用户导入频率
     *
     * @param userId 用户ID；为 null 或 <=0 时不做限制（未登录场景）
     */
    void checkRateLimit(Long userId);
}
