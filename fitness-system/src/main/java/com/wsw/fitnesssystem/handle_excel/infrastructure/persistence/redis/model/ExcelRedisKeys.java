package com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.redis.model;

/**
 * Excel 导入模块 Redis Key 规范
 * <p>设计原则（参照 AuthRedisKeys）：</p>
 * <ul>
 *     <li>前缀区分业务域：excel:{子系统}:{业务}:{维度}</li>
 *     <li>任务维度：所有导入进度 Key 必须包含 taskId，便于隔离和排查</li>
 *     <li>生命周期分离：导入任务 TTL 24h，避免 Redis 堆积</li>
 * </ul>
 * @author loriyuhv
 * @version 1.0 2026/8/22 11:10
 * @since 1.0
 */
public class ExcelRedisKeys {

    private ExcelRedisKeys() {}

    // ==================== 导入任务进度 ====================

    /**
     * 导入任务进度 Hash
     * <li>Key: excel:import:task:{taskId}</li>
     * <li>Field: locked / total / processed / successCount / failCount / errorMsg</li>
     * <li>TTL: 24小时</li>
     */
    private static final String IMPORT_TASK_PREFIX = "excel:import:task:";

    /**
     * 导入任务进度 Key
     * @param taskId 任务ID
     * @return Redis Key
     */
    public static String importTaskKey(String taskId) {
        return IMPORT_TASK_PREFIX + taskId;
    }

    // ==================== 导入限流（可选扩展）====================

    /**
     * 用户导入频率限制（String）
     * <li>Key: excel:limit:import:{userId}</li>
     * <li>Value: 计数</li>
     * <li>TTL: 1分钟</li>
     */
    private static final String LIMIT_IMPORT_PREFIX = "excel:limit:import:";

    public static String limitImportKey(Long userId) {
        return LIMIT_IMPORT_PREFIX + userId;
    }

    // ==================== 导入分布式锁（可选扩展）====================

    /**
     * 导入任务分布式锁（防止同一文件重复提交）
     * <li>Key: excel:lock:import:{fileMd5}</li>
     * <li>TTL: 30秒</li>
     */
    private static final String LOCK_IMPORT_PREFIX = "excel:lock:import:";

    public static String lockImportKey(String fileMd5) {
        return LOCK_IMPORT_PREFIX + fileMd5;
    }
}
