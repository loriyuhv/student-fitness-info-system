package com.wsw.fitnesssystem.handle_excel.application;

import com.wsw.fitnesssystem.handle_excel.core.adapter.BusinessAdapterFactory;
import com.wsw.fitnesssystem.handle_excel.core.adapter.ImportAdapter;
import com.wsw.fitnesssystem.handle_excel.core.executor.ImportTaskExecutor;
import com.wsw.fitnesssystem.handle_excel.domain.enums.ExcelBizTypeEnum;
import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ExcelConstants;
import com.wsw.fitnesssystem.handle_excel.interfaces.dto.ImportProgressDTO;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Excel 导入应用服务
 * <p>统一入口：接收请求 → 文件转存 → 获取适配器 → 提交线程池 → 返回 taskId</p>
 * <p>原则：只编排，不写业务逻辑</p>
 * @author loriyuhv
 * @version 1.0 2026/8/21 14:29
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportAppService {
    private final BusinessAdapterFactory adapterFactory;
    private final ImportTaskExecutor taskExecutor;
    private final ImportProgressQueryService progressQueryService;

    /**
     * 统一导入入口
     *
     * @param bizTypeEnum 业务类型枚举
     * @param file    上传的 Excel 文件
     * @return taskId 任务 ID，用于后续查询进度
     */
    public String importExcel(ExcelBizTypeEnum bizTypeEnum, MultipartFile file) {
        // 1. 校验文件
        validateFile(file);

        // 2. 校验 bizType 并获取适配器（提前校验，避免转存后才发现类型错误）
        ImportAdapter<?, ?> adapter = adapterFactory.getImportAdapter(bizTypeEnum.getCode());

        // 3. 转存临时文件（同步线程完成，解决 MultipartFile InputStream 异步关闭问题）
        String taskId = UUID.randomUUID().toString();
        File tempFile = saveTempFile(file, taskId);

        // 4. 提交异步任务（传文件路径 + 适配器）
        taskExecutor.submit(taskId, tempFile, adapter);

        log.info("[{}] 导入任务已提交, bizType={}", taskId, bizTypeEnum.getCode());

        // 5. 立即返回 taskId，HTTP 线程释放
        return taskId;
    }

    /**
     * 查询导入进度
     */
    public ImportProgressDTO getProgress(String taskId) {
        return progressQueryService.getProgress(taskId);
    }

    /**
     * 获取所有已注册的导入类型（用于前端下拉选择）
     */
    public List<String> getAllBizTypes() {
        return adapterFactory.getAllBizTypes();
    }

    // ========== 私有方法 ==========

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.PARAM_INVALID, "上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BizException(ResultCode.PARAM_INVALID, "文件名不能为空");
        }
        String lowerName = originalFilename.toLowerCase();
        boolean validExt = false;
        for (String ext : ExcelConstants.ALLOWED_EXTENSIONS) {
            if (lowerName.endsWith(ext)) {
                validExt = true;
                break;
            }
        }
        if (!validExt) {
            throw new BizException(ResultCode.PARAM_TYPE_ERROR, "仅支持 .xlsx / .xls 格式，当前文件: " + originalFilename);
        }
        // 限制单文件大小 50MB
        if (file.getSize() > ExcelConstants.MAX_FILE_SIZE) {
            throw new BizException(ResultCode.PARAM_INVALID, "文件大小超过 50MB 限制");
        }
    }

    private File saveTempFile(MultipartFile file, String taskId) {
        // 按日期分片存储，避免单目录文件过多
        String dateDir = java.time.LocalDate.now().toString();
        File tempDir = new File(
                System.getProperty("java.io.tmpdir"),
                ExcelConstants.TEMP_DIR_ROOT + "/" + dateDir + "/" + taskId
        );
        if (!tempDir.mkdirs()) {
            log.warn("临时目录已存在或创建失败: {}", tempDir.getAbsolutePath());
        }

        File tempFile = new File(tempDir, ExcelConstants.TEMP_FILE_NAME);
        try {
            file.transferTo(tempFile);
            log.debug("[{}] 文件转存成功: {}", taskId, tempFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("[{}] 文件转存失败", taskId, e);
            // throw new BizException(ResultCode.SYSTEM_ERROR, "文件转存失败: " + e.getMessage());
            throw new BizException(ResultCode.SYSTEM_ERROR);
        }
        return tempFile;
    }
}
