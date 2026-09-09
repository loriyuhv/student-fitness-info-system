package com.wsw.fitnesssystem.data_exchange.domain.model;

import com.wsw.fitnesssystem.data_exchange.domain.enums.ImportStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入任务聚合根。
 * <p>
 * 封装导入任务的生命周期与状态流转，保证业务规则一致性。
 * 状态流转：{@link ImportStatus#INIT} → {@link ImportStatus#PROCESSING}
 * → {@link ImportStatus#FINISHED}/{@link ImportStatus#PARTIAL}/{@link ImportStatus#FAILED}/{@link ImportStatus#CANCELLED}
 * </p>
 *
 * <p><b>构造约束：</b></p>
 * <ul>
 *   <li>新建任务：使用 {@link #ImportTask(String)}</li>
 *   <li>重建任务（仅供 Repository 使用）：使用 {@link #ImportTask(String, ImportStatus, int, int, int, int, List, String)}</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/7 20:32
 * @since 1.0
 */
@Getter
public class ImportTask {

    /** 错误摘要最大保留条数 */
    private final int MAX_ERROR_SUMMARY_SIZE = 20;

    /** 任务唯一标识 */
    private final String taskId;

    /** 当前任务状态 */
    private ImportStatus status;

    /** 总数据条数 */
    private int total;

    /** 已处理条数（成功 + 失败） */
    private int processed;

    /** 成功条数 */
    private int successCount;

    /** 失败条数 */
    private int failCount;

    /** 错误摘要列表（最多保留 20 条，供前端展示） */
    private List<String> errorSummary;

    /**
     * 错误文件路径（仅当 {@link #status} 为 PARTIAL 时才有值）。
     * <p>
     * 该字段由应用层在生成错误文件后通过 {@link #setErrorFilePath} 设置，
     * 领域层本身不负责错误文件的生成。
     */
    @Setter
    private String errorFilePath;

    // ==================== 构造函数 ====================

    /**
     * 创建新任务（初始状态为 {@link ImportStatus#INIT}）。
     *
     * @param taskId 任务唯一标识
     */
    public ImportTask(String taskId) {
        this.taskId = taskId;
        this.status = ImportStatus.INIT;
        this.errorSummary = new ArrayList<>();
    }

    /**
     * 重建构造函数（Redis 恢复聚合根）
     * <p>
     * <b>警告：仅供 Repository 层内部使用，应用层不应直接调用此构造方法。</b>
     * 应用层请使用 {@link #ImportTask(String)} 创建新任务。
     *
     * @param taskId 任务ID
     * @param status 任务状态
     * @param total 总数
     * @param processed 已处理数
     * @param successCount 成功数
     * @param failCount 失败数
     * @param errorSummary 错误摘要
     * @param errorFilePath 错误文件路径
     */
    public ImportTask(
        String taskId, ImportStatus status, int total, int processed,
        int successCount, int failCount, List<String> errorSummary, String errorFilePath
    ) {
        this.taskId = taskId;
        this.status = status;
        this.total = total;
        this.processed = processed;
        this.successCount = successCount;
        this.failCount = failCount;
        this.errorSummary = errorSummary != null ? new ArrayList<>(errorSummary) : new ArrayList<>();
        this.errorFilePath = errorFilePath;
    }

    // ==================== 行为方法 ====================

    /**
     * 启动任务，状态由 INIT → PROCESSING。
     *
     * @param total 总数据条数
     * @throws IllegalStateException 若非 INIT 状态则抛出
     */
    public void start(int total) {
        if (this.status != ImportStatus.INIT) {
            throw new IllegalStateException("任务已启动，不能重复启动");
        }
        this.total = total;
        this.status = ImportStatus.PROCESSING;
    }

    /**
     * 记录单批处理结果，累加计数并追加错误摘要。
     *
     * @param batchSuccess 本批成功条数
     * @param batchFail    本批失败条数
     * @param batchErrors  本批错误摘要（可为空）
     * @throws IllegalStateException 若非 PROCESSING 状态则抛出
     */
    public void recordBatch(int batchSuccess, int batchFail, List<String> batchErrors) {
        if (this.status != ImportStatus.PROCESSING) {
            throw new IllegalStateException("任务未在处理中状态");
        }
        this.successCount += batchSuccess;
        this.failCount += batchFail;
        this.processed += (batchSuccess + batchFail);
        if (batchErrors != null && !batchErrors.isEmpty()) {
            this.errorSummary.addAll(batchErrors);
            // 限制错误摘要数量，防止过大
            if (this.errorSummary.size() > MAX_ERROR_SUMMARY_SIZE) {
                this.errorSummary = this.errorSummary.subList(0, MAX_ERROR_SUMMARY_SIZE);
            }
        }
    }

    /**
     * 全部成功完成，状态 → FINISHED。
     *
     * @throws IllegalStateException 若非 PROCESSING 状态则抛出
     */
    public void finishSuccess() {
        if (this.status != ImportStatus.PROCESSING) {
            throw new IllegalStateException("任务未在处理中状态");
        }
        this.status = ImportStatus.FINISHED;
    }

    /**
     * 部分成功完成（存在失败记录），状态 → PARTIAL。
     *
     * @throws IllegalStateException 若非 PROCESSING 状态则抛出
     */
    public void finishPartial() {
        if (this.status != ImportStatus.PROCESSING) {
            throw new IllegalStateException("任务未在处理中状态");
        }
        this.status = ImportStatus.PARTIAL;
    }

    /**
     * 标记任务失败（系统级异常），状态 → FAILED，覆盖错误摘要。
     *
     * @param errorMsg 错误信息
     */
    public void fail(String errorMsg) {
        this.status = ImportStatus.FAILED;
        this.errorSummary = List.of(errorMsg);
    }

    /**
     * 用户主动取消任务，状态 → CANCELLED。
     */
    public void cancel() {
        this.status = ImportStatus.CANCELLED;
        this.errorSummary = List.of("Task cancelled by user");
    }

    // ==================== 查询方法 ====================

    /**
     * 判断任务是否正在运行（INIT 或 PROCESSING）。
     *
     * @return true 表示运行中
     */
    public boolean isRunning() {
        return this.status == ImportStatus.PROCESSING || this.status == ImportStatus.INIT;
    }

    /**
     * 计算导入进度百分比。
     *
     * @return 0 ~ 100 的整数百分比
     */
    public int getPercent() {
        if (total <= 0) return 0;
        return Math.min(100, (int) ((processed * 100.0) / total));
    }

}
