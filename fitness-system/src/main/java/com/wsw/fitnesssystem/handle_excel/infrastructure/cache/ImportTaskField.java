package com.wsw.fitnesssystem.handle_excel.infrastructure.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 导入任务进度 Hash 的 Field 常量。
 * <p>
 * 配合 {@code import:task:{taskId}} 使用，统一定义所有 Hash Field。
 * 避免硬编码字符串，享受 IDE 自动补全与编译期检查。
 * </p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/22 11:38
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum ImportTaskField {

    /** 任务状态 */
    STATUS("status"),

    /** 总数据条数 */
    TOTAL("total"),

    /** 已处理条数（成功 + 失败） */
    PROCESSED("processed"),

    /** 成功条数 */
    SUCCESS_COUNT("successCount"),

    /** 失败条数 */
    FAIL_COUNT("failCount"),

    /** 错误摘要（截断后存入） */
    ERROR_MSG("errorMsg"),

    /** 取消标记（1 表示已取消） */
    CANCELLED("cancelled"),

    /** 错误文件磁盘路径 */
    ERROR_FILE_PATH("errorFilePath");

    private final String key;

    @Override
    public String toString() {
        return key;
    }

}
