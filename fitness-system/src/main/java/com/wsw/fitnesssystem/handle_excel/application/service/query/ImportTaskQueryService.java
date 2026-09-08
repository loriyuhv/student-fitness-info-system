package com.wsw.fitnesssystem.handle_excel.application.service.query;

import com.wsw.fitnesssystem.handle_excel.application.dto.result.ImportProgressResult;
import com.wsw.fitnesssystem.handle_excel.domain.model.ImportTask;
import com.wsw.fitnesssystem.handle_excel.domain.repository.ImportTaskRepository;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 导入任务查询服务（只读）
 *
 * @author loriyuhv
 * @version 1.0 2026/9/8 12:36
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportTaskQueryService {

    private final ImportTaskRepository importTaskRepository;

    public ImportProgressResult getProgress(String taskId) {
        return importTaskRepository.findById(taskId)
            .map(this::buildResult)
            .orElseThrow(
                () -> new BizException(ResultCode.IMPORT_TASK_NOT_FOUND, "Task not found: " + taskId)
            );
    }

    public String getErrorFilePath(String taskId) {
        return importTaskRepository.findById(taskId)
            .map(ImportTask::getErrorFilePath)
            .orElse(null);
    }

    // ======================= 辅助方法 ============================

    private ImportProgressResult buildResult(ImportTask task) {
        return ImportProgressResult.builder()
            .total(task.getTotal())
            .processed(task.getProcessed())
            .successCount(task.getSuccessCount())
            .failCount(task.getFailCount())
            .status(task.getStatus())
            .errorMsg(task.getErrorSummary().isEmpty()
                ? "" : String.join(" | ", task.getErrorSummary())
            )
            .errorFileExists(task.getErrorFilePath() != null)
            .percent(task.getPercent())
            .build();
    }

}
