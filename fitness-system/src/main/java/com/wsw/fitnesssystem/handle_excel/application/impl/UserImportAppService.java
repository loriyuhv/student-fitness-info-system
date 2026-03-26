package com.wsw.fitnesssystem.handle_excel.application.impl;

import com.wsw.fitnesssystem.handle_excel.application.IUserImportAppService;
import com.wsw.fitnesssystem.handle_excel.infrastructure.async.ImportTaskExecutor;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.redis.ImportProgressRepository;
import com.wsw.fitnesssystem.handle_excel.interfaces.dto.ImportProgressDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:43
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class UserImportAppService implements IUserImportAppService {
    private final ImportTaskExecutor executor;
    private final ImportProgressRepository progressRepository;

    @Override
    public String importUsers(MultipartFile file) {
        String taskId = UUID.randomUUID().toString();

        executor.submit(() -> executor.doImport(taskId, file));
        // executor.doImport(taskId, file);
        return taskId;
    }

    @Override
    public ImportProgressDTO getProgress(String taskId) {
        Map<Object, Object> data = progressRepository.getProgress(taskId);
        return ImportProgressDTO.from(data);
    }
}
