package com.wsw.fitnesssystem.handle_excel.application.service.command;

import com.wsw.fitnesssystem.handle_excel.application.plugin.ImportPluginRegistry;
import com.wsw.fitnesssystem.handle_excel.application.plugin.ImportPlugin;
import com.wsw.fitnesssystem.handle_excel.application.scheduler.AsyncImportScheduler;
import com.wsw.fitnesssystem.handle_excel.application.port.output.DistributedLockPort;
import com.wsw.fitnesssystem.handle_excel.application.port.output.RateLimiterPort;
import com.wsw.fitnesssystem.handle_excel.infrastructure.util.FileCleanupUtils;
import com.wsw.fitnesssystem.handle_excel.application.enums.ImportBizType;
import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ImportConfig;
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
import java.util.Arrays;
import java.util.UUID;

/**
 * 导入提交应用服务。
 * <p>职责：文件转存 → 限流校验 → 防重校验 → 提交异步线程池 → 返回任务ID。</p>
 * <p>
 * <b>职责边界：</b>仅编排用例流程，不包含具体业务逻辑，不直接操作持久化层。
 * 所有外部依赖通过端口（Port）调用，符合六边形架构的依赖倒置原则。
 * </p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 14:29
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportSubmissionService {

    private final ImportPluginRegistry pluginRegistry;
    private final AsyncImportScheduler importScheduler;
    private final RateLimiterPort rateLimiterPort;
    private final DistributedLockPort distributedLockPort;

    /**
     * 提交导入任务。
     *
     * @param bizTypeEnum 业务类型枚举
     * @param file    上传的文件
     * @param userId 操作人ID（用于频率限制）
     * @return 任务 ID，用于后续查询进度
     */
    public String submit(ImportBizType bizTypeEnum, MultipartFile file, Long userId) {
        // 1. 前置校验：文件格式、大小、扩展名
        validateFile(file);

        // 2. 限流：防止单用户高频提交
        rateLimiterPort.checkRateLimit(userId);

        // 3. 获取业务插件（提前校验，避免文件转存后才发现不支持）
        ImportPlugin<?, ?> plugin = pluginRegistry.getPlugin(bizTypeEnum.getCode());

        // 4. 转存临时文件（同步操作，避免异步线程读取时 MultipartFile 已关闭）
        String taskId = UUID.randomUUID().toString();
        File tempFile = saveTempFile(file, taskId);

        // 5. 防重检查：相同 MD5 的文件不能并发导入
        String md5 = computeFileMd5(tempFile);
        boolean locked = distributedLockPort.tryLock(md5, taskId);
        if (!locked) {
            // 防重失败：清理已转存的临时文件，避免磁盘泄漏
            FileCleanupUtils.cleanup(tempFile);
            log.warn("[{}] Duplicate file submission rejected, md5={}, userId={}", taskId, md5, userId);
            throw new BizException(ResultCode.PARAM_INVALID, "该文件正在导入中，请勿重复提交");
        }

        // 6. 提交异步任务（文件路径 + 业务插件 + MD5）
        importScheduler.submit(taskId, tempFile, plugin, md5);
        log.info("[{}] Import task submitted, bizType={}, userId={}, md5={}",
            taskId, bizTypeEnum.getCode(), userId, md5);

        // 7. 立即返回 taskId，HTTP 线程释放
        return taskId;
    }

    // ================================================================
    //  私有方法
    // ================================================================

    /**
     * 校验上传文件：非空、文件名、扩展名、大小。
     *
     * @param file 待校验的文件
     * @throws BizException 校验失败时抛出
     */
    private void validateFile(MultipartFile file) {
        // 1. 非空校验（包含空文件）
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.PARAM_INVALID, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        // 2. 文件名校验（防御性检查，理论上不会为 null）
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BizException(ResultCode.PARAM_INVALID, "文件名不能为空");
        }

        // 3. 扩展名校验（取真实后缀，防止 file.xlsx.exe 绕过）
        String ext = extractExtension(originalFilename);
        boolean validExt = false;
        for (String allowed : ImportConfig.ALLOWED_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(ext)) {
                validExt = true;
                break;
            }
        }
        if (!validExt) {
            throw new BizException(
                ResultCode.PARAM_TYPE_ERROR,
                "不支持的文件格式，仅允许" + Arrays.toString(ImportConfig.ALLOWED_EXTENSIONS)
            );
        }

        // 4. 大小校验（默认 200MB）
        if (file.getSize() > ImportConfig.MAX_FILE_SIZE) {
            throw new BizException(ResultCode.PARAM_INVALID,
                "文件大小超过限制（最大 " + (ImportConfig.MAX_FILE_SIZE / 1024 / 1024) + "MB）"
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
            ImportConfig.TEMP_DIR_ROOT + "/" + dateDir + "/" + taskId
        );

        // 如果目录不存在且创建失败则抛异常
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            log.warn("Failed to create temp directory: {}", tempDir.getAbsolutePath());
            throw new BizException(ResultCode.SYSTEM_ERROR, "创建临时目录失败");
        }

        File tempFile = new File(tempDir, ImportConfig.TEMP_FILE_NAME);
        try {
            file.transferTo(tempFile);
            log.debug("[{}] File saved to temp location: {}", taskId, tempFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("[{}] Failed to save temp file", taskId, e);
            throw new BizException(ResultCode.FILE_UPLOAD_ERROR, "文件转存失败: " + e.getMessage());
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
            throw new BizException(ResultCode.SYSTEM_ERROR, "文件 MD5 计算失败");
        }
    }

}
