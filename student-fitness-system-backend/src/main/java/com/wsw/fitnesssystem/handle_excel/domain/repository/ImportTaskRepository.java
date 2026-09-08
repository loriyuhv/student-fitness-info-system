package com.wsw.fitnesssystem.handle_excel.domain.repository;

import com.wsw.fitnesssystem.handle_excel.domain.model.ImportTask;

import java.util.Optional;

/**
 * 导入任务仓储接口（领域层出口）
 * <p>职责：管理 {@link ImportTask} 聚合根的持久化与查询</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/23 01:07
 * @since 1.0
 */
public interface ImportTaskRepository {

    /**
     * 保存或更新整个聚合根 （全量覆盖）
     * @param task 聚合根对象
     */
    void save(ImportTask task);

    /**
     * 根据任务ID查询聚合根
     * @param taskId 任务ID
     * @return 聚合根（可能为空）
     */
    Optional<ImportTask> findById(String taskId);

    /**
     * 请求取消任务（设置取消标记，供异步线程轮询）
     * @param taskId 任务ID
     */
    void requestCancel(String taskId);

    /**
     * 检查任务是否已被取消
     * @param taskId 任务ID
     * @return true 表示已被取消
     */
    boolean isCancelled(String taskId);

}
