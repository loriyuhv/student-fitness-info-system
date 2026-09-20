package com.wsw.fitnesssystem.data_exchange.infrastructure.storage;

import com.wsw.fitnesssystem.data_exchange.application.dto.upload.UploadedFile;
import com.wsw.fitnesssystem.data_exchange.application.port.output.FileStoragePort;
import com.wsw.fitnesssystem.data_exchange.infrastructure.config.ImportInfrastructureProperties;
import com.wsw.fitnesssystem.shared.application.exception.SystemException;
import com.wsw.fitnesssystem.shared.interfaces.web.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
public class FileStorageLocalAdapter implements FileStoragePort {

    private final ImportInfrastructureProperties infraProps;

    /**
     * 将上传文件转存为本地临时文件。
     *
     * <p>按日期分目录存储，避免单目录下文件过多影响性能。
     * 临时文件路径：{@code {java.io.tmpdir}/excel-import/{date}/{taskId}/data.xlsx}</p>
     *
     * @param file   上传文件（应用层抽象，含输入流）
     * @param taskId 任务 ID（用于隔离不同任务的临时目录）
     * @return 转存后的临时文件对象
     * @throws SystemException 目录创建失败或文件写入失败
     */
    @Override
    public File saveTempFile(UploadedFile file, String taskId) {
        // 1. 按日期分片存储，避免单目录文件过多
        String dateDir = LocalDate.now().toString();
        File tempDir = new File(
            System.getProperty("java.io.tmpdir"),
            infraProps.getTempFile().getRootDir() + "/" + dateDir + "/" + taskId
        );

        // 2. 目录不存在则创建
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            log.error("Failed to create temp directory, path={}", tempDir.getAbsolutePath());
            throw new SystemException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        // 3. 通过输入流拷贝（UploadedFile 无 transferTo，统一用流式复制）
        File tempFile = new File(tempDir, infraProps.getTempFile().getFileName());
        try (InputStream in = file.openInputStream()) {
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.debug("[{}] File saved to temp location: {}", taskId, tempFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("[{}] Failed to save temp file, path={}", taskId, tempFile.getAbsolutePath(), e);
            throw new SystemException(ErrorCode.FILE_UPLOAD_ERROR, e);
        }

        return tempFile;
    }

    @Override
    public String computeFileMd5(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.md5DigestAsHex(fis);
        } catch (IOException e) {
            log.error("Failed to compute MD5, path={}", file.getAbsolutePath(), e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    @Override
    public void cleanup(File file) {
        if (file == null) {
            return;
        }

        try {
            // 1. 删除临时文件
            if (file.exists()) {
                FileUtils.delete(file);
                log.debug("Temp file deleted: {}", file.getAbsolutePath());
            }

            // 2. 删除任务目录（{rootDir}/{date}/{taskId}）
            File taskDir = file.getParentFile();
            if (taskDir != null && taskDir.exists()) {
                FileUtils.deleteDirectory(taskDir);
                log.debug("Temp task directory deleted: {}", taskDir.getAbsolutePath());

                // 3. 尝试清理空的日期目录（{rootDir}/{date}）
                File dateDir = taskDir.getParentFile();
                if (dateDir != null && dateDir.exists() && isDirectoryEmpty(dateDir)) {
                    FileUtils.deleteDirectory(dateDir);
                    log.debug("Empty date directory deleted: {}", dateDir.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            // 清理失败不阻断主流程，仅告警
            log.warn("Failed to clean up temp files, path={}", file.getAbsolutePath(), e);
        }
    }

    /**
     * 判断目录是否为空（仅含空子目录时也算空）。
     */
    private boolean isDirectoryEmpty(File dir) {
        String[] children = dir.list();
        return children == null || children.length == 0;
    }

}
