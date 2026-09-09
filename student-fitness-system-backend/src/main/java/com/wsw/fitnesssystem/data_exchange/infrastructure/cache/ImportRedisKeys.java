package com.wsw.fitnesssystem.data_exchange.infrastructure.cache;

import com.wsw.fitnesssystem.data_exchange.infrastructure.config.ImportInfrastructureProperties;

/**
 * 导入模块 Redis Key 规范。
 * <p>
 * <b>设计原则：</b>
 * <ul>
 *   <li>统一前缀 {@code import:}，隔离不同业务域</li>
 *   <li>所有 Key 必须包含业务标识（taskId 或 userId），便于追踪与清理</li>
 *   <li>设置合理 TTL，避免 Redis 内存堆积</li>
 * </ul>
 * <p>
 * <b>Key 命名规范：</b>
 * <pre>
 * import:{模块}:{维度}:{标识}
 *
 * 示例：
 *   import:task:{taskId}          → 任务进度
 *   import:lock:file:{md5}        → 文件防重锁
 *   import:limit:user:{userId}    → 用户限流
 * </pre>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/22 11:10
 * @since 1.0
 */
public class ImportRedisKeys {

    private ImportRedisKeys() {}

    // ================================================================
    //  1. 导入任务进度（Hash 结构）
    // ================================================================

    /** 任务进度 Key 前缀 */
    private static final String TASK_PREFIX = "import:task:";

    /**
     * 导入任务进度 Key。
     * <p>
     * <b>数据结构：</b>Hash
     * <pre>
     * Key: import:task:{taskId}
     * Fields:
     *   status          → 任务状态（PROCESSING/FINISHED/PARTIAL/FAILED/CANCELLED）
     *   total           → 总数据条数
     *   processed       → 已处理条数
     *   successCount    → 成功条数
     *   failCount       → 失败条数
     *   errorMsg        → 错误摘要（最多 20 条）
     *   errorFilePath   → 错误文件磁盘路径
     *   cancelled       → 取消标记（1 表示已取消，用于线程轮询）
     * </pre>
     * <b>TTL：</b>24 小时（由 {@link ImportInfrastructureProperties#getRedis()} 控制）
     *
     * @param taskId 任务 ID
     * @return Redis Key
     */
    public static String taskKey(String taskId) {
        return TASK_PREFIX + taskId;
    }

    // ================================================================
    //  2. 文件防重锁（String 结构）
    // ================================================================

    /** 文件锁 Key 前缀 */
    private static final String FILE_LOCK_PREFIX = "import:lock:file:";

    /**
     * 文件防重锁 Key。
     * <p>
     * <b>数据结构：</b>String
     * <pre>
     * Key: import:lock:file:{fileMd5}
     * Value: {taskId}  → 持有锁的任务 ID
     * </pre>
     * 基于文件 MD5 实现防重，同一文件同时只能有一个导入任务。
     * <b>TTL：</b>60 分钟（由 {@link ImportInfrastructureProperties#getRedis()} 控制，作为兜底释放）
     *
     * @param fileMd5 文件 MD5
     * @return Redis Key
     */
    public static String fileLockKey(String fileMd5) {
        return FILE_LOCK_PREFIX + fileMd5;
    }

    // ================================================================
    //  3. 用户限流（String 结构）
    // ================================================================

    /** 限流 Key 前缀 */
    private static final String RATE_LIMIT_PREFIX = "import:limit:user:";

    /**
     * 用户导入限流 Key。
     * <p>
     * <b>数据结构：</b>String（计数器）
     * <pre>
     * Key: import:limit:user:{userId}
     * Value: {count}  → 时间窗口内提交次数
     * </pre>
     * 防止单用户高频提交，保护系统资源。
     * <b>TTL：</b>60 秒（由 {@link ImportInfrastructureProperties#getRateLimit()} 控制）
     *
     * @param userId 用户 ID
     * @return Redis Key
     */
    public static String rateLimitKey(Long userId) {
        return RATE_LIMIT_PREFIX + userId;
    }

}
