package com.wsw.fitnesssystem.data_exchange.application.service.command;

import com.wsw.fitnesssystem.data_exchange.domain.model.ImportTask;
import com.wsw.fitnesssystem.data_exchange.domain.repository.ImportTaskRepository;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 导入任务命令服务（写操作）
 *
 * @author loriyuhv
 * @version 1.0 2026/9/8 12:50
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportTaskCommandService {

    private final ImportTaskRepository importTaskRepository;

    public void cancelTask(String taskId) {
        Optional<ImportTask> optional = importTaskRepository.findById(taskId);
        if (optional.isEmpty()) {
            throw new BizException(ResultCode.IMPORT_TASK_NOT_FOUND, "Task not found: " + taskId);
        }
        ImportTask task = optional.get();
        if (!task.isRunning()) {
            throw new BizException(ResultCode.PARAM_INVALID, "Task is not running");
        }
        importTaskRepository.requestCancel(taskId);
        log.info("Cancellation requested for task: {}", taskId);
    }

}
