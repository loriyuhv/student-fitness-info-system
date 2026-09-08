package com.wsw.fitnesssystem.handle_excel.interfaces;

import com.wsw.fitnesssystem.handle_excel.application.service.ImportSubmissionService;
import com.wsw.fitnesssystem.handle_excel.application.service.ImportProgressQueryService;
import com.wsw.fitnesssystem.handle_excel.application.ImportTemplateAppService;
import com.wsw.fitnesssystem.handle_excel.application.enums.ImportBizType;
import com.wsw.fitnesssystem.handle_excel.domain.enums.ImportStatus;
import com.wsw.fitnesssystem.handle_excel.interfaces.dto.ImportProgressResponse;
import com.wsw.fitnesssystem.shared.context.RequestContextHolder;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ApiResult;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 导入控制器。
 * <p>
 * 提供导入提交、进度查询、取消、模板下载等 REST 接口。
 * </p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 15:35
 * @since 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/excel")
public class ImportController {

    private final ImportSubmissionService importSubmissionService;
    private final ImportTemplateAppService importTemplateAppService;
    private final ImportProgressQueryService importProgressQueryService;

    /**
     * 提交导入任务。
     * <p>提交Excel文件，根据业务类型执行对应解析导入逻辑，任务后台异步执行，不会阻塞HTTP请求</p>
     *
     * @param bizType 业务类型，区分导入数据类型，例如：USER_IMPORT(用户导入)、FITNESS_RECORD_IMPORT(体测数据导入)
     * @param file 待导入的Excel文件
     * @return ApiResult 返回异步任务taskId，用于后续查询导入进度
     */
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResult<String> importExcel(
            @RequestParam String bizType,
            @RequestParam("file") MultipartFile file
    ) {
        // 字符串转枚举，非法参数直接抛出异常
        ImportBizType bizTypeEnum = ImportBizType.getByCode(bizType);
        Operator operator = RequestContextHolder.getRequiredOperator();
        String taskId = importSubmissionService.importExcel(bizTypeEnum, file, operator.userId());
        return ApiResult.success(taskId);
    }

    /**
     * 查询导入任务进度
     * @param taskId 异步导入任务编号，由{@link #importExcel(String, MultipartFile)}接口返回
     * @return ApiResult<ImportProgressResponse> 返回任务进度DTO，包含总条数、成功数、失败数、错误信息、任务状态
     */
    @GetMapping("/import/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResult<ImportProgressResponse> getProgress(@RequestParam String taskId) {
        ImportProgressResponse dto = importProgressQueryService.getProgress(taskId);
        if (dto.getStatus() == ImportStatus.INIT) {
            throw new BizException(ResultCode.IMPORT_TASK_NOT_FOUND, "Task not found" + taskId);
        }
        return ApiResult.success(dto);
    }

    /**
     * 获取全部支持导入的业务类型列表
     * <p>前端下拉框可直接使用该返回值，动态展示可导入选项</p>
     * @return ApiResult<List<String>> 支持的bizType业务类型集合
     */
    @GetMapping("/import/types")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResult<List<String>> getImportTypes() {
        return ApiResult.success(importProgressQueryService.getAllBizTypes());
    }

    /**
     * 错误文件下载接口
     * @param taskId TokenID
     * @param response 响应
     */
    @GetMapping("/import/errors/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public void downloadErrorFile(@RequestParam String taskId, HttpServletResponse response) {
        String filePath = importProgressQueryService.getErrorFilePath(taskId);
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
    @PostMapping("/import/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResult<String> cancelImport(@RequestParam String taskId) {
        importProgressQueryService.cancelTask(taskId);
        return ApiResult.success("Cancellation request submitted");
    }

    /**
     * 下载导入模板
     *
     * @param bizType  业务类型（如 USER_IMPORT）
     * @param response HTTP 响应
     */
    @GetMapping("/import/template")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public void downloadTemplate(@RequestParam String bizType, HttpServletResponse response) {
        // 校验 bizType 是否合法（利用枚举校验）
        ImportBizType.getByCode(bizType);
        // 调用应用服务生成并下载
        importTemplateAppService.downloadTemplate(bizType, response);
    }

}
