package com.wsw.fitnesssystem.handle_excel.application.impl;

import com.wsw.fitnesssystem.handle_excel.application.IUserImportAppService;
import com.wsw.fitnesssystem.handle_excel.infrastructure.async.ImportTaskExecutor;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.redis.ImportProgressRepository;
import com.wsw.fitnesssystem.handle_excel.interfaces.dto.ImportProgressDTO;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:43
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserImportAppService implements IUserImportAppService {
    private final ImportTaskExecutor executor;
    private final ImportProgressRepository progressRepository;

    @Override
    public String importUsers(MultipartFile file) {
        // 1. 校验文件
        if (file == null || file.isEmpty()) {
            // throw new BizException(ResultCode.PARAM_INVALID, "上传文件不能为空");
            throw new BizException(ResultCode.PARAM_INVALID);
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
                !(originalFilename.endsWith(".xlsx") ||
                        originalFilename.endsWith(".xlx"))) {
            throw new BizException(ResultCode.PARAM_TYPE_ERROR);
        }

        String taskId = UUID.randomUUID().toString();

        // 2. 同步线程：转存临时文件（解决 MultipartFile InputStream 异步关闭问题）
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "import/" + taskId);
        boolean isMade = tempDir.mkdirs();
        if (!isMade) {
            return null;
        }
        File tempFile = new File(tempDir, "users.xlsx");
        try {
            file.transferTo(tempFile); // 同步完成
        } catch (IOException e) {
            log.error("文件转存失败, taskId={}", taskId, e);
            throw new BizException(ResultCode.SYSTEM_ERROR);
        }

        // 3. 提交异步任务，传递路径而非MultipartFile
        executor.submit(taskId, tempFile.getAbsolutePath());
        // executor.submit(() -> executor.doImport(taskId, file));
        // executor.doImport(taskId, file);

        // 4. 立即返回 taskId，HTTP 线程释放
        return taskId;
    }

    @Override
    public ImportProgressDTO getProgress(String taskId) {
        return progressRepository.getProgress(taskId);
    }
}
