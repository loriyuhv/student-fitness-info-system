package com.wsw.fitnesssystem.handle_excel.infrastructure.config;

import lombok.NoArgsConstructor;

/**
 * Excel 导入模块常量配置
 * 集中管理所有硬编码参数，便于统一调整和后续配置化
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 22:42
 * @since 1.0
 */
@NoArgsConstructor
public class ExcelConstants {
    // ==================== 文件限制 ====================
    /**
     * 单文件最大大小：50MB
     */
    public static final long MAX_FILE_SIZE = 50 * 1024 * 1024L;

    /**
     * 支持的文件扩展名
     */
    public static final String[] ALLOWED_EXTENSIONS = {".xlsx", ".xls"};

    // ==================== 批量处理 ====================
    /**
     * 默认每批处理条数
     */
    public static final int DEFAULT_BATCH_SIZE = 3000;

    /**
     * 小文件阈值（< 此条数用全量解析，≥ 此条数用流式解析）
     */
    public static final int STREAM_THRESHOLD = 10_000;

    /**
     * 批量插入内部分片大小（防止 SQL 过长）
     */
    public static final int DB_BATCH_SIZE = 3000;

    // ==================== 用户导入默认值 ====================
    /** 默认校区 ID */
    public static final Long DEFAULT_CAMPUS_ID = 1001L;

    /** 默认用户类型：2-学生 */
    public static final Integer DEFAULT_USER_TYPE = 2;

    /** 默认状态：1-启用 */
    public static final Integer DEFAULT_STATUS = 1;

    /** 默认删除标记：0-未删除 */
    public static final Integer DEFAULT_DELETED = 0;

    /** 默认密码（空密码时使用） */
    public static final String DEFAULT_PASSWORD = "123456";

    /** 用户名最大长度 */
    public static final int USERNAME_MAX_LENGTH = 50;

    // ==================== Redis ====================

    /** 导入任务进度 TTL：24 小时 */
    public static final long IMPORT_TASK_TTL_HOURS = 24;

    /** 错误信息最大长度（存入 Redis） */
    public static final int ERROR_MSG_MAX_LENGTH = 500;

    /** 错误信息保留条数 */
    public static final int ERROR_MSG_MAX_COUNT = 3;

    // ==================== 临时文件 ====================

    /** 临时文件根目录 */
    public static final String TEMP_DIR_ROOT = "excel-import";

    /** 临时文件名 */
    public static final String TEMP_FILE_NAME = "data.xlsx";
}
