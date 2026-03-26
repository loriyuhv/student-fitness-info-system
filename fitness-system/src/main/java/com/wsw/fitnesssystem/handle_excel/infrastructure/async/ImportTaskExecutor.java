package com.wsw.fitnesssystem.handle_excel.infrastructure.async;

import com.wsw.fitnesssystem.handle_excel.application.dto.UserExcelDTO;
import com.wsw.fitnesssystem.handle_excel.domain.model.User;
import com.wsw.fitnesssystem.handle_excel.domain.service.UserImportDomainService;
import com.wsw.fitnesssystem.handle_excel.infrastructure.excel.ExcelParser;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.UserBatchRepository;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.assembler.UserAssembler;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.entity.SysUser;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.redis.ImportProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:51
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImportTaskExecutor {
    private final ExcelParser excelParser;
    private final UserImportDomainService domainService;
    private final UserBatchRepository userBatchRepository;
    private final ImportProgressRepository progressRepository;
    private final UserAssembler userAssembler;

    private static final int BATCH_SIZE = 500;

    public void submit(Runnable task) {
        CompletableFuture.runAsync(task);
    }

    public void doImport(String taskId, MultipartFile file) {

        progressRepository.init(taskId);

        List<UserExcelDTO> list = excelParser.parse(file);

        int total = list.size();
        progressRepository.setTotal(taskId, total);

        int processed = 0;

        for (List<UserExcelDTO> batch : partition(list, BATCH_SIZE)) {

            List<User> users = domainService.validateAndConvert(batch);

            List<SysUser> entities = userAssembler.toEntityList(users);

            userBatchRepository.batchInsert(entities);

            processed += batch.size();

            progressRepository.updateProgress(taskId, processed);
        }

        progressRepository.finish(taskId);
    }

    private List<List<UserExcelDTO>> partition(List<UserExcelDTO> list, int size) {
        List<List<UserExcelDTO>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            result.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return result;
    }
}
