package com.wsw.fitnesssystem.data_exchange.application.dto;

import com.wsw.fitnesssystem.data_exchange.domain.enums.ImportStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/8 12:39
 * @since 1.0
 */
@Getter
@Builder
public class ImportProgressResult {

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
     * 百分比
     */
    private int percent;

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
