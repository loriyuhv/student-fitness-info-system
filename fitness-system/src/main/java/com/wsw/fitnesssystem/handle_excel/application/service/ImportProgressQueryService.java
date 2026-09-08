package com.wsw.fitnesssystem.handle_excel.application.service;

import com.wsw.fitnesssystem.handle_excel.application.plugin.ImportPluginRegistry;
import com.wsw.fitnesssystem.handle_excel.domain.enums.ImportStatus;
import com.wsw.fitnesssystem.handle_excel.domain.model.ImportTask;
import com.wsw.fitnesssystem.handle_excel.domain.repository.ImportTaskRepository;
import com.wsw.fitnesssystem.handle_excel.interfaces.dto.ImportProgressResponse;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 导入进度查询应用服务
 * 可扩展：增加本地缓存、降级策略等
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 14:30
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportProgressQueryService {

    private final ImportPluginRegistry pluginRegistry;
    private final ImportTaskRepository importTaskRepository;

    /**
     * 查询导入任务进度
     *
     * @param taskId 任务ID
     * @return 导入进度状态DTO
     */
    public ImportProgressResponse getProgress(String taskId) {
        return importTaskRepository.findById(taskId)
            .map(this::toDTO)
            .orElseGet(this::notFoundDTO);
    }

    /**
     * 获取所有已注册的导入类型（用于前端下拉选择）
     */
    public List<String> getAllBizTypes() {
        return pluginRegistry.getAllBizTypes();
    }

    /**
     * 获取错误文件路径。
     */
    public String getErrorFilePath(String taskId) {
        return importTaskRepository.findById(taskId)
            .map(ImportTask::getErrorFilePath)
            .orElse(null);
    }

    /**
     * 取消任务（如果任务正在运行）。
     */
    public void cancelTask(String taskId) {
        // 1. 检查任务是否存在
        Optional<ImportTask> optional = importTaskRepository.findById(taskId);
        if (optional.isEmpty()) {
            throw new BizException(ResultCode.IMPORT_TASK_NOT_FOUND, "Task not found: " + taskId);
        }
        ImportTask task = optional.get();
        if (!task.isRunning()) {
            throw new BizException(ResultCode.PARAM_INVALID, "Task is not running");
        }
        // 2. 只有正在运行的任务才能取消（INIT 或 PROCESSING）
        importTaskRepository.requestCancel(taskId);
        log.info("Cancellation requested for task: {}", taskId);
    }

    // ==================== DTO 转换 ====================

    /**
     * 将聚合根转换为进度 DTO（可复用）
     * @param task 任务状态
     * @return 转换后的DTO
     */
    private ImportProgressResponse toDTO(ImportTask task) {
        ImportProgressResponse dto = new ImportProgressResponse();
        dto.setTotal(task.getTotal());
        dto.setProcessed(task.getProcessed());
        dto.setSuccessCount(task.getSuccessCount());
        dto.setFailCount(task.getFailCount());
        dto.setStatus(task.getStatus());
        dto.setErrorMsg(task.getErrorSummary().isEmpty()
            ? "" : String.join(" | ", task.getErrorSummary()));
        dto.setErrorFileExists(task.getErrorFilePath() != null);
        return dto;
    }

    /**
     * 任务状态未找到
     * @return 空DTO
     */
    private ImportProgressResponse notFoundDTO() {
        ImportProgressResponse dto = new ImportProgressResponse();
        dto.setStatus(ImportStatus.NOT_FOUND);
        return dto;
    }

}
