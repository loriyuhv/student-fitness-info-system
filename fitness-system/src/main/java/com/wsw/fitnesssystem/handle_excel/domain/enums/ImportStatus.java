package com.wsw.fitnesssystem.handle_excel.domain.enums;

/**
 * Excel 导入任务状态
 * @author loriyuhv
 * @version 1.0 2026/8/22 11:40
 * @since 1.0
 */
public enum ImportStatus {
    INIT,           // 初始
    PROCESSING,     // 处理中
    FINISHED,       // 全部成功
    PARTIAL,        // 部分成功
    FAILED,         // 失败
    NOT_FOUND;      // 记录不存在（仅 DTO 层使用，不入库）

    public boolean isCompleted() {
        return this == FINISHED || this == PARTIAL;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isRunning() {
        return this == INIT || this == PROCESSING;
    }
}
