package com.wsw.fitnesssystem.handle_excel.application;

import com.wsw.fitnesssystem.handle_excel.core.adapter.BusinessAdapterFactory;
import com.wsw.fitnesssystem.handle_excel.core.progress.ImportProgressManager;
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

    private final BusinessAdapterFactory adapterFactory;
    private final ImportProgressManager progressManager;

    /**
     * 查询Excel文件导入任务进度
     * @param taskId 任务ID
     * @return 导入进度状态DTO
     */
    public ImportProgressDTO getProgress(String taskId) {
        return progressManager.getProgress(taskId);
    }

    /**
     * 获取所有已注册的导入类型（用于前端下拉选择）
     */
    public List<String> getAllBizTypes() {
        return adapterFactory.getAllBizTypes();
    }
}
