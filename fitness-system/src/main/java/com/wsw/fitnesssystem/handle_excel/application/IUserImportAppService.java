package com.wsw.fitnesssystem.handle_excel.application;

import com.wsw.fitnesssystem.handle_excel.interfaces.dto.ImportProgressDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:42
 * @since 1.0
 */
public interface IUserImportAppService {
    String importUsers(MultipartFile file);

    ImportProgressDTO getProgress(String taskId);
}
