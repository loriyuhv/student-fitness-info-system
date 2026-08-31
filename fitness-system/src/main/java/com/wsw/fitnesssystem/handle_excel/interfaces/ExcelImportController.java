package com.wsw.fitnesssystem.handle_excel.interfaces;

import com.wsw.fitnesssystem.handle_excel.application.ExcelImportAppService;
import com.wsw.fitnesssystem.handle_excel.application.ExcelImportProgressQueryAppService;
import com.wsw.fitnesssystem.handle_excel.core.port.ImportProgressPort;
import com.wsw.fitnesssystem.handle_excel.domain.enums.ExcelBizTypeEnum;
import com.wsw.fitnesssystem.handle_excel.domain.enums.ImportStatus;
import com.wsw.fitnesssystem.handle_excel.interfaces.dto.ImportProgressDTO;
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
 * Excel导入对外接口控制器
 * <p>提供Excel文件异步导入、导入进度查询、支持导入业务类型查询接口</p>
 * <p>导入为异步任务，上传文件后返回任务ID，客户端通过taskId轮询获取导入进度与结果</p>
 * @author loriyuhv
 * @version 1.0 2026/8/21 15:35
 * @since 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/excel")
public class ExcelImportController {

    private final ImportProgressPort importProgressPort;
    private final ExcelImportAppService importAppService;
    private final ExcelImportProgressQueryAppService importProgressQueryAppService;

    /**
     * Excel文件异步导入接口
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
        ExcelBizTypeEnum bizTypeEnum = ExcelBizTypeEnum.getByCode(bizType);
        Operator operator = RequestContextHolder.getRequiredOperator();
        String taskId = importAppService.importExcel(bizTypeEnum, file, operator.userId());
        return ApiResult.success(taskId);
    }

    /**
     * 查询Excel导入任务进度
     * @param taskId 异步导入任务编号，由{@link #importExcel(String, MultipartFile)}接口返回
     * @return ApiResult<ImportProgressDTO> 返回任务进度DTO，包含总条数、成功数、失败数、错误信息、任务状态
     */
    @GetMapping("/import/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResult<ImportProgressDTO> getProgress(@RequestParam String taskId) {
        return ApiResult.success(importProgressQueryAppService.getProgress(taskId));
    }

    /**
     * 获取全部支持导入的业务类型列表
     * <p>前端下拉框可直接使用该返回值，动态展示可导入选项</p>
     * @return ApiResult<List<String>> 支持的bizType业务类型集合
     */
    @GetMapping("/import/types")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResult<List<String>> getImportTypes() {
        return ApiResult.success(importProgressQueryAppService.getAllBizTypes());
    }

    /**
     * 错误文件下载接口
     * @param taskId TokenID
     * @param response 响应
     */
    @GetMapping("/import/errors/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public void downloadErrorFile(@RequestParam String taskId, HttpServletResponse response) {
        String filePath = importProgressPort.getErrorFilePath(taskId);
        if (filePath == null) {
            throw new BizException(ResultCode.FILE_NOT_FOUND, "暂无错误文件");
        }
        File file = new File(filePath);
        if (!file.exists()) {
            throw new BizException(ResultCode.FILE_NOT_FOUND, "错误文件已过期或被清理");
        }
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=errors_" + taskId + ".xlsx");
            java.nio.file.Files.copy(file.toPath(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            log.error("下载错误文件失败", e);
            throw new BizException(ResultCode.SYSTEM_ERROR, "下载失败");
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
        // 1. 检查任务是否存在
        ImportProgressDTO progress = importProgressPort.getProgress(taskId);
        if (progress.getStatus() == null || progress.getStatus() == ImportStatus.NOT_FOUND) {
            throw new BizException(ResultCode.IMPORT_TASK_NOT_FOUND, "Task not found: " + taskId);
        }

        // 2. 只有正在运行的任务才能取消（INIT 或 PROCESSING）
        if (!progress.isRunning()) {
            throw new BizException(ResultCode.PARAM_INVALID,
                "Task is not running, current status: " + progress.getStatus());
        }

        // 3. 请求取消
        importProgressPort.requestCancel(taskId);
        log.info("User requested cancellation for task: {}", taskId);

        return ApiResult.success("Cancellation request submitted");
    }

}
