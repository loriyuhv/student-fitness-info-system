package com.wsw.fitnesssystem.handle_excel.infrastructure.config;

import lombok.NoArgsConstructor;

/**
 * 导入模块配置。
 * <p>
 * 集中管理文件限制、批量处理、缓存策略、限流规则、校验规则等所有配置项。
 * 调整配置无需修改业务代码，便于运维调优。
 * </p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 22:42
 * @since 1.0
 */
@NoArgsConstructor
public class ImportConfig {

    // ================================================================
    //  1. 文件限制
    // ================================================================

    /** 单文件最大大小：200MB */
    public static final long MAX_FILE_SIZE = 200 * 1024 * 1024L;

    /** 支持的文件扩展名 */
    public static final String[] ALLOWED_EXTENSIONS = {".xlsx", ".xls"};

    // ================================================================
    //  2. 批量处理
    // ================================================================

    /** 默认每批处理条数 */
    public static final int DEFAULT_BATCH_SIZE = 500;

    /** 小文件阈值：< 此值走全量解析，≥ 此值走流式解析 */
    public static final int STREAM_THRESHOLD = 10_000;

    // ================================================================
    //  3. 用户导入默认值
    // ================================================================

    /** 用户名最大长度 */
    public static final int USERNAME_MAX_LENGTH = 50;

    /** 密码最小长度 */
    public static final int PASSWORD_MIN_LENGTH = 6;

    /** 昵称最大长度 */
    public static final int NICKNAME_MAX_LENGTH = 20;

    // ================================================================
    //  4. 缓存策略（Redis）
    // ================================================================

    /** 导入任务进度 TTL：24 小时（任务过期自动清理） */
    public static final long IMPORT_TASK_TTL_HOURS = 24;

    /** 错误信息最大长度（存入 Redis 时截断） */
    public static final int ERROR_MSG_MAX_LENGTH = 500;

    /** 错误摘要保留条数（前端展示，完整错误下载错误文件查看） */
    public static final int ERROR_MSG_MAX_COUNT = 20;

    // ================================================================
    //  5. 锁策略（Redis）
    // ================================================================

    /** 文件锁 TTL：60 分钟（正常由任务完成释放，TTL 仅作兜底） */
    public static final long FILE_LOCK_TTL_MINUTES = 60L;

    // ================================================================
    //  6. 限流策略
    // ================================================================

    /** 导入限制时间窗口：60 秒 */
    public static final int RATE_LIMIT_WINDOW_SECONDS = 60;

    /** 导入限制窗口内最大允许次数 */
    public static final int RATE_LIMIT_MAX_COUNT = 5;

    // ================================================================
    //  7. 临时文件
    // ================================================================

    /** 临时文件根目录（相对于 java.io.tmpdir） */
    public static final String TEMP_DIR_ROOT = "import";

    /** 临时文件名 */
    public static final String TEMP_FILE_NAME = "data.xlsx";

}
