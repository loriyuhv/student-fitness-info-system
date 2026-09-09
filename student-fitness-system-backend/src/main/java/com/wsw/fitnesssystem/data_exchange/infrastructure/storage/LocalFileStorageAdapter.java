package com.wsw.fitnesssystem.data_exchange.infrastructure.storage;

import com.wsw.fitnesssystem.data_exchange.application.port.output.FileStoragePort;
import com.wsw.fitnesssystem.data_exchange.infrastructure.config.ImportInfrastructureProperties;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;

/**
 * 本地文件存储适配器（输出端口 {@link FileStoragePort} 的实现）。
 * <p>
 * 将上传的文件转存到本地临时目录，并提供文件清理和 MD5 计算能力。
 * </p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/9 14:56
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalFileStorageAdapter implements FileStoragePort {

    private final ImportInfrastructureProperties infraProps;

    /**
     * 将上传的 MultipartFile 转存为本地临时文件
     *
     * <p>按日期分目录存储，避免单目录下文件过多影响性能。
     * 临时文件路径：{@code {java.io.tmpdir}/excel-import/{date}/{taskId}/data.xlsx}</p>
     *
     * @param file   上传的文件
     * @param taskId 任务 ID（用于隔离不同任务的临时目录）
     * @return 转存后的临时文件对象
     */
    @Override
    public File saveTempFile(MultipartFile file, String taskId) {
        // 按日期分片存储，避免单目录文件过多
        String dateDir = LocalDate.now().toString();
        File tempDir = new File(
            System.getProperty("java.io.tmpdir"),
            infraProps.getTempFile().getRootDir() + "/" + dateDir + "/" + taskId
        );

        // 如果目录不存在且创建失败则抛异常
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            log.warn("Failed to create temp directory: {}", tempDir.getAbsolutePath());
            throw new BizException(ResultCode.SYSTEM_ERROR, "创建临时目录失败");
        }

        File tempFile = new File(tempDir, infraProps.getTempFile().getFileName());
        try {
            file.transferTo(tempFile);
            log.debug("[{}] File saved to temp location: {}", taskId, tempFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("[{}] Failed to save temp file", taskId, e);
            throw new BizException(ResultCode.FILE_UPLOAD_ERROR, "文件转存失败: " + e.getMessage());
        }

        return tempFile;
    }

    @Override
    public String computeFileMd5(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.md5DigestAsHex(fis);
        } catch (IOException e) {
            log.error("Failed to compute MD5 for file: {}", file.getAbsolutePath(), e);
            throw new BizException(ResultCode.SYSTEM_ERROR, "文件 MD5 计算失败");
        }
    }

    @Override
    public void cleanup(File file) {
        if (file == null) {
            return;
        }

        try {
            if (file.exists()) {
                FileUtils.delete(file);
                log.debug("Temp file deleted: {}", file.getAbsolutePath());
            }

            File parent = file.getParentFile();
            if (parent != null && parent.exists()) {
                FileUtils.deleteDirectory(parent);
                log.debug("Temp directory deleted: {}", parent.getAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("Failed to clean up temp files, path={}", file.getAbsolutePath(), e);
        }
    }

}
