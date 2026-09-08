package com.wsw.fitnesssystem.handle_excel.domain.model;

import com.wsw.fitnesssystem.handle_excel.domain.enums.ImportStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/7 20:32
 * @since 1.0
 */
@Getter
public class ImportTask {

    private final String taskId;
    private ImportStatus status;
    private int total;
    private int processed;
    private int successCount;
    private int failCount;
    private List<String> errorSummary;

    /** 注意：setErrorFilePath 用于基础设施层设置路径 */
    @Setter
    private String errorFilePath;

    /**
     * 构造函数（新建任务时调用）
     * @param taskId 任务唯一标识
     */
    public ImportTask(String taskId) {
        this.taskId = taskId;
        this.status = ImportStatus.INIT;
        this.errorSummary = new ArrayList<>();
    }


    /**
     * 重建构造函数（仅供 Repository 层调用，从 Redis 恢复聚合根）
     * 包级私有，不允许应用层直接调用
     *
     * @param taskId 任务ID
     * @param status 任务状态
     * @param total 总数
     * @param processed 进度
     * @param successCount 成功数
     * @param failCount 失败数
     * @param errorSummary 错误列表
     * @param errorFilePath 错误文件路径
     */
    public ImportTask(
        String taskId, ImportStatus status, int total, int processed,
        int successCount, int failCount, List<String> errorSummary, String errorFilePath) {

        this.taskId = taskId;
        this.status = status;
        this.total = total;
        this.processed = processed;
        this.successCount = successCount;
        this.failCount = failCount;
        this.errorSummary = errorSummary != null ? new ArrayList<>(errorSummary) : new ArrayList<>();
        this.errorFilePath = errorFilePath;

    }

    /**
     * 行为：启动任务
     * @param total 总数
     */
    public void start(int total) {
        if (this.status != ImportStatus.INIT) {
            throw new IllegalStateException("Task already started");
        }
        this.total = total;
        this.status = ImportStatus.PROCESSING;
    }

    /**
     * 行为：记录一批结果
     * @param batchSuccess 成功批次数
     * @param batchFail 失败批次数
     * @param batchErrors 批次错误列表
     */
    public void recordBatch(int batchSuccess, int batchFail, List<String> batchErrors) {
        if (this.status != ImportStatus.PROCESSING) {
            throw new IllegalStateException("Task is not in processing state");
        }
        this.successCount += batchSuccess;
        this.failCount += batchFail;
        this.processed += (batchSuccess + batchFail);
        if (batchErrors != null && !batchErrors.isEmpty()) {
            this.errorSummary.addAll(batchErrors);
            // 限制错误摘要数量，防止过大
            if (this.errorSummary.size() > 100) {
                this.errorSummary = this.errorSummary.subList(0, 100);
            }
        }
    }

    // --- 行为：全部成功 ---
    public void finishSuccess() {
        if (this.status != ImportStatus.PROCESSING) {
            throw new IllegalStateException("Cannot finish a non-processing task");
        }
        this.status = ImportStatus.FINISHED;
    }

    // --- 行为：部分成功（有失败记录） ---
    public void finishPartial() {
        if (this.status != ImportStatus.PROCESSING) {
            throw new IllegalStateException("Cannot finish a non-processing task");
        }
        this.status = ImportStatus.PARTIAL;
    }

    // --- 行为：任务失败（系统异常） ---
    public void fail(String errorMsg) {
        this.status = ImportStatus.FAILED;
        this.errorSummary = List.of(errorMsg);
    }

    // --- 行为：用户取消 ---
    public void cancel() {
        this.status = ImportStatus.CANCELLED;
        this.errorSummary = List.of("Task cancelled by user");
    }

    // --- 工具方法 ---
    public boolean isRunning() {
        return this.status == ImportStatus.PROCESSING || this.status == ImportStatus.INIT;
    }

    public int getPercent() {
        if (total <= 0) return 0;
        return Math.min(100, (int) ((processed * 100.0) / total));
    }

}
