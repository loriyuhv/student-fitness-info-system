package com.wsw.fitnesssystem.data_exchange.interfaces;

import com.wsw.fitnesssystem.data_exchange.application.dto.result.ImportProgressResult;
import com.wsw.fitnesssystem.data_exchange.application.service.command.ImportSubmissionService;
import com.wsw.fitnesssystem.data_exchange.application.ImportTemplateAppService;
import com.wsw.fitnesssystem.data_exchange.application.enums.ImportBizType;
import com.wsw.fitnesssystem.data_exchange.application.service.command.ImportTaskCommandService;
import com.wsw.fitnesssystem.data_exchange.application.service.query.ImportTaskQueryService;
import com.wsw.fitnesssystem.data_exchange.application.service.query.ImportTypeQueryService;
import com.wsw.fitnesssystem.data_exchange.interfaces.dto.ImportProgressResponse;
import com.wsw.fitnesssystem.shared.context.RequestContextHolder;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ApiResult;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 导入控制器。
 * <p>提供导入提交、进度查询、取消、模板下载等 REST 接口。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 15:35
 * @since 1.0
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/import")
public class ImportController {

    private final ImportTaskQueryService importTaskQueryService;
    private final ImportTypeQueryService importTypeQueryService;
    private final ImportSubmissionService importSubmissionService;
    private final ImportTaskCommandService importTaskCommandService;
    private final ImportTemplateAppService importTemplateAppService;

    /**
     * 提交导入任务。
     * <p>上传 Excel 文件并指定业务类型，系统异步执行导入，立即返回任务 ID。</p>
     *
     * @param bizType 业务类型（如 USER_IMPORT、FITNESS_RECORD_IMPORT）
     * @param file    待导入的 Excel 文件（.xlsx / .xls，最大 50MB）
     * @return 任务 ID（taskId），用于后续查询进度或取消任务
     */
    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResult<String> submit(
            @RequestParam @NotBlank(message = "业务类型不能为空") String bizType,
            @RequestParam MultipartFile file
    ) {
        // 1. 业务类型校验并转枚举（无效类型抛出 BizException）
        ImportBizType bizTypeEnum = ImportBizType.getByCode(bizType);

        // 2. 从安全上下文中获取当前操作人信息（含 userId）
        Operator operator = RequestContextHolder.getRequiredOperator();

        // 3. 提交导入任务到异步线程池，返回任务 ID
        String taskId = importSubmissionService.submit(bizTypeEnum, file, operator.userId());

        return ApiResult.success(taskId);
    }

    /**
     * 查询导入任务进度
     * @param taskId 异步导入任务编号，由{@link #submit(String, MultipartFile)}接口返回
     * @return ApiResult<ImportProgressResponse> 返回任务进度DTO，包含总条数、成功数、失败数、错误信息、任务状态
     */
    @GetMapping("/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResult<ImportProgressResponse> getProgress(@RequestParam String taskId) {
        ImportProgressResult result = importTaskQueryService.getProgress(taskId);
        return ApiResult.success(buildResponse(result));
    }

    /**
     * 获取全部支持导入的业务类型列表
     * <p>前端下拉框可直接使用该返回值，动态展示可导入选项</p>
     * @return ApiResult<List<String>> 支持的bizType业务类型集合
     */
    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResult<List<String>> getImportTypes() {
        return ApiResult.success(importTypeQueryService.getAllBizTypes());
    }

    /**
     * 错误文件下载接口
     * @param taskId TokenID
     * @param response 响应
     */
    @GetMapping("/errors/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public void downloadErrorFile(@RequestParam String taskId, HttpServletResponse response) {
        String filePath = importTaskQueryService.getErrorFilePath(taskId);
        if (filePath == null) {
            throw new BizException(ResultCode.FILE_NOT_FOUND, "No error file for this task");
        }
        File file = new File(filePath);
        if (!file.exists()) {
            throw new BizException(ResultCode.FILE_NOT_FOUND, "Error file expired or removed");
        }
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=errors_" + taskId + ".xlsx");
            java.nio.file.Files.copy(file.toPath(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            log.error("Failed to download error file: taskId={}", taskId, e);
            throw new BizException(ResultCode.SYSTEM_ERROR, "Error file download failed");
        }
    }

    /**
     * 取消导入任务
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    @PostMapping("/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResult<String> cancelImport(@RequestParam String taskId) {
        importTaskCommandService.cancelTask(taskId);
        return ApiResult.success("Cancellation request submitted");
    }

    /**
     * 下载导入模板
     *
     * @param bizType  业务类型（如 USER_IMPORT）
     * @param response HTTP 响应
     */
    @GetMapping("/template")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public void downloadTemplate(@RequestParam String bizType, HttpServletResponse response) {
        // 校验 bizType 是否合法（利用枚举校验）
        ImportBizType.getByCode(bizType);
        // 调用应用服务生成并下载
        importTemplateAppService.downloadTemplate(bizType, response);
    }

    // ======================= 辅助方法 ============================

    private ImportProgressResponse buildResponse(ImportProgressResult result) {
        return ImportProgressResponse.builder()
            .total(result.getTotal())
            .processed(result.getProcessed())
            .successCount(result.getSuccessCount())
            .failCount(result.getFailCount())
            .status(result.getStatus().name())
            .errorMsg(result.getErrorMsg())
            .errorFileExists(result.isErrorFileExists())
            .percent(result.getPercent())
            .completed(result.isCompleted())
            .running(result.isRunning())
            .failed(result.isFailed())
            .build();
    }

}
