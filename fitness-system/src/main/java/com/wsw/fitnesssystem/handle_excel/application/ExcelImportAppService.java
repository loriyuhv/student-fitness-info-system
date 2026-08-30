package com.wsw.fitnesssystem.handle_excel.application;

import com.wsw.fitnesssystem.handle_excel.core.adapter.BusinessAdapterFactory;
import com.wsw.fitnesssystem.handle_excel.core.adapter.ImportAdapter;
import com.wsw.fitnesssystem.handle_excel.core.executor.ImportTaskExecutor;
import com.wsw.fitnesssystem.handle_excel.core.port.ImportFileLockPort;
import com.wsw.fitnesssystem.handle_excel.core.port.ImportRateLimitPort;
import com.wsw.fitnesssystem.handle_excel.core.utils.FileCleanupUtils;
import com.wsw.fitnesssystem.handle_excel.domain.enums.ExcelBizTypeEnum;
import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ExcelConstants;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Excel 导入应用服务
 * <p>统一入口：文件转存 → 限流校验 → 防重校验 → 提交线程池 → 返回 taskId</p>
 * <p>原则：只编排用例，不写业务逻辑，不碰 Redis/数据库</p>
 *
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
    private final ImportRateLimitPort importRateLimitPort;
    private final ImportFileLockPort importFileLockPort;

    /**
     * 统一导入入口
     *
     * @param bizTypeEnum 业务类型枚举
     * @param file    上传的 Excel 文件
     * @param userId 当前登录用户ID（用于频率限制）
     * @return taskId 任务 ID，用于后续查询进度
     */
    public String importExcel(ExcelBizTypeEnum bizTypeEnum, MultipartFile file, Long userId) {
        // 1. 校验文件格式/大小
        validateFile(file);

        // 2. 用户频率限制
        importRateLimitPort.checkRateLimit(userId);

        // 3. 校验 bizType 并获取适配器（提前校验，避免转存后才发现类型错误）
        ImportAdapter<?, ?> adapter = adapterFactory.getImportAdapter(bizTypeEnum.getCode());

        // 4. 转存临时文件（同步线程完成，解决 MultipartFile InputStream 异步关闭问题）
        String taskId = UUID.randomUUID().toString();
        File tempFile = saveTempFile(file, taskId);

        // 5. 计算文件 MD5 并防重检查
        String md5 = computeFileMd5(tempFile);
        boolean locked = importFileLockPort.tryLock(md5, taskId);
        if (!locked) {
            // 防重失败：清理已转存的临时文件，避免磁盘泄漏
            FileCleanupUtils.cleanup(tempFile);
            log.warn("[{}] Duplicate file submission rejected, md5={}, userId={}", taskId, md5, userId);
            throw new BizException(
                ResultCode.PARAM_INVALID,
                "This file is already being imported, please do not submit again"
            );
        }

        // 6. 提交异步任务（传文件路径 + 适配器）（携带 md5，任务完成后 finally 释放锁）
        taskExecutor.submit(taskId, tempFile, adapter, md5);
        log.info("[{}] Import task submitted, bizType={}, userId={}, md5={}",
            taskId, bizTypeEnum.getCode(), userId, md5);

        // 7. 立即返回 taskId，HTTP 线程释放
        return taskId;
    }

    // ========== 私有方法 ==========

    /**
     * 校验上传文件
     * <p>校验项：</p>
     * <li>1. 文件非空</li>
     * <li>2. 文件名非空</li>
     * <li>3. 文件扩展名合法（.xlsx / .xls）</li>
     * <li>4. 文件大小不超过 50MB</li>
     *
     * @param file 待校验的文件
     * @throws BizException 校验失败时抛出
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.PARAM_INVALID, "Upload file cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BizException(ResultCode.PARAM_INVALID, "File name cannot be empty");
        }

        // FIX: 取真实扩展名，防止 file.xlsx.exe 绕过校验
        String ext = extractExtension(originalFilename);
        boolean validExt = false;
        for (String allowed : ExcelConstants.ALLOWED_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(ext)) {
                validExt = true;
                break;
            }
        }
        if (!validExt) {
            throw new BizException(ResultCode.PARAM_TYPE_ERROR,
                "Unsupported file format: " + originalFilename + ". Only .xlsx and .xls are allowed"
            );
        }

        // 限制单文件大小 例如50MB
        if (file.getSize() > ExcelConstants.MAX_FILE_SIZE) {
            throw new BizException(ResultCode.PARAM_INVALID,
                "File size exceeds " + (ExcelConstants.MAX_FILE_SIZE / 1024 / 1024) + "MB limit"
            );
        }
    }

    /**
     * 提取文件扩展名（含点），如 ".xlsx"
     * @param filename 文件名称
     * @return 真实扩展名
     */
    private String extractExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot == -1 ? "" : filename.substring(lastDot).toLowerCase();
    }

    /**
     * 将上传的 MultipartFile 转存为本地临时文件
     *
     * <p>按日期分目录存储，避免单目录下文件过多影响性能。
     * 临时文件路径：{@code {java.io.tmpdir}/excel-import/{date}/{taskId}/data.xlsx}</p>
     *
     * @param file 上传的 Excel 文件
     * @param taskId 任务 ID，用于隔离不同任务的临时目录
     * @return 转存后的临时文件对象
     * @throws BizException 当无法创建临时目录或文件转存失败时抛出
     */
    private File saveTempFile(MultipartFile file, String taskId) {
        // 按日期分片存储，避免单目录文件过多
        String dateDir = java.time.LocalDate.now().toString();
        File tempDir = new File(
                System.getProperty("java.io.tmpdir"),
                ExcelConstants.TEMP_DIR_ROOT + "/" + dateDir + "/" + taskId
        );

        // 如果目录不存在且创建失败则抛异常
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            log.warn("Failed to create temp directory: {}", tempDir.getAbsolutePath());
            throw new BizException(ResultCode.SYSTEM_ERROR,
                "Unable to create temp directory: " + tempDir.getAbsolutePath()
            );
        }

        File tempFile = new File(tempDir, ExcelConstants.TEMP_FILE_NAME);
        try {
            file.transferTo(tempFile);
            log.debug("[{}] File saved to temp location: {}", taskId, tempFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("[{}] Failed to save temp file", taskId, e);
            throw new BizException(ResultCode.SYSTEM_ERROR, "Failed to save temp file: " + e.getMessage());
        }

        return tempFile;
    }

    /**
     * 计算文件 MD5（纯计算，无外部依赖，放在 AppService 内部即可）
     *
     * @param file 要计算 MD5 的文件
     * @return 文件的 MD5 十六进制字符串
     * @throws BizException 当文件读取失败时抛出
     */
    private String computeFileMd5(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.md5DigestAsHex(fis);
        } catch (IOException e) {
            log.error("Failed to compute MD5 for file: {}", file.getAbsolutePath(), e);
            throw new BizException(ResultCode.SYSTEM_ERROR, "Failed to compute file MD5: " + e.getMessage());
        }
    }

}
