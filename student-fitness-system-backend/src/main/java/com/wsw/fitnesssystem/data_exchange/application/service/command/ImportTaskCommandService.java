package com.wsw.fitnesssystem.data_exchange.application.service.command;

import com.wsw.fitnesssystem.data_exchange.domain.model.ImportTask;
import com.wsw.fitnesssystem.data_exchange.domain.repository.ImportTaskRepository;
import com.wsw.fitnesssystem.data_exchange.error.DataExchangeErrorCode;
import com.wsw.fitnesssystem.shared.application.exception.BizException;
import com.wsw.fitnesssystem.shared.kernel.error.CommonErrorCode;
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
            throw new BizException(DataExchangeErrorCode.IMPORT_TASK_NOT_FOUND, "任务未找到：" + taskId);
        }
        ImportTask task = optional.get();
        if (task.isTerminated()) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "任务已结束，无法取消");
        }
        importTaskRepository.requestCancel(taskId);
        log.info("Cancellation requested for task: {}", taskId);
    }

}
