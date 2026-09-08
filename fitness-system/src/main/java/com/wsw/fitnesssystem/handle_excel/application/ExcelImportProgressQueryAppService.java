package com.wsw.fitnesssystem.handle_excel.application;

import com.wsw.fitnesssystem.handle_excel.application.plugin.ImportPluginRegistry;
import com.wsw.fitnesssystem.handle_excel.domain.enums.ImportStatus;
import com.wsw.fitnesssystem.handle_excel.domain.model.ImportTask;
import com.wsw.fitnesssystem.handle_excel.domain.repository.ImportTaskRepository;
import com.wsw.fitnesssystem.handle_excel.interfaces.dto.ImportProgressDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 导入进度查询应用服务
 * 可扩展：增加本地缓存、降级策略等
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 14:30
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class ExcelImportProgressQueryAppService {

    private final ImportPluginRegistry adapterFactory;
    private final ImportTaskRepository importTaskRepository;

    /**
     * 查询Excel文件导入任务进度
     * @param taskId 任务ID
     * @return 导入进度状态DTO
     */
    public ImportProgressDTO getProgress(String taskId) {
        return importTaskRepository.findById(taskId)
            .map(this::toDTO)
            .orElseGet(() -> {
                ImportProgressDTO dto = new ImportProgressDTO();
                dto.setStatus(ImportStatus.NOT_FOUND);
                return dto;
            });
    }

    /**
     * 获取所有已注册的导入类型（用于前端下拉选择）
     */
    public List<String> getAllBizTypes() {
        return adapterFactory.getAllBizTypes();
    }

    private ImportProgressDTO toDTO(ImportTask task) {
        ImportProgressDTO dto = new ImportProgressDTO();
        dto.setTotal(task.getTotal());
        dto.setProcessed(task.getProcessed());
        dto.setSuccessCount(task.getSuccessCount());
        dto.setFailCount(task.getFailCount());
        dto.setStatus(task.getStatus());
        dto.setErrorMsg(task.getErrorSummary().isEmpty() ? "" : String.join(" | ", task.getErrorSummary()));
        dto.setErrorFileExists(task.getErrorFilePath() != null);
        return dto;
    }

}
