package com.wsw.fitnesssystem.handle_excel.application;

import com.wsw.fitnesssystem.handle_excel.core.progress.ImportProgressManager;
import com.wsw.fitnesssystem.handle_excel.interfaces.dto.ImportProgressDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 导入进度查询服务
 * 可扩展：增加本地缓存、降级策略等
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 14:30
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class ImportProgressQueryService {
    private final ImportProgressManager progressManager;

    public ImportProgressDTO getProgress(String taskId) {
        return progressManager.getProgress(taskId);
    }
}
