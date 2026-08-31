package com.wsw.fitnesssystem.handle_excel.interfaces.dto;

import com.wsw.fitnesssystem.handle_excel.domain.enums.ImportStatus;
import lombok.Data;

/**
 * 导入进度 DTO
 * 用于前端展示导入状态
 *
 * @author loriyuhv
 * @version 1.0 2026/3/26 16:09
 * @since 1.0
 */
@Data
public class ImportProgressDTO {

    /**
     * 总数据条数
     */
    private int total;

    /**
     * 已处理条数（成功 + 失败）
     */
    private int processed;

    /**
     * 成功条数
     */
    private int successCount;

    /**
     * 失败条数
     */
    private int failCount;

    /**
     * 状态：INIT / PROCESSING / FINISHED / PARTIAL / FAILED / NOT_FOUND
     */
    private ImportStatus status;

    /**
     * 错误信息摘要（最多前3条）
     */
    private String errorMsg;

    /**
     * 是否有错误文件可下载
     */
    private boolean errorFileExists;

    /**
     * 计算进度百分比
     * @return 百分比
     */
    public int getPercent() {
        if (total <= 0) return 0;
        return Math.min(100, (int) ((processed * 100.0) / total));
    }

    /**
     * 是否已完成（成功或部分成功）
     * @return 标志
     */
    public boolean isCompleted() {
        return status != null && status.isCompleted();
    }

    /**
     * 是否失败
     * @return 失败
     */
    public boolean isFailed() {
        return status != null && status.isFailed();
    }

    /**
     * 是否运行
     * @return true 运行
     */
    public boolean isRunning() {
        return status != null && status.isRunning();
    }

}
