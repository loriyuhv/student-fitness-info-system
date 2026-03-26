package com.wsw.fitnesssystem.handle_excel.interfaces;

import com.wsw.fitnesssystem.handle_excel.application.IUserImportAppService;
import com.wsw.fitnesssystem.shared.response.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:41
 * @since 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserImportController {
    private final IUserImportAppService userImportAppService;

    @PostMapping("/import")
    public ApiResult<String> importUsers(@RequestParam MultipartFile file) {
        return ApiResult.success(userImportAppService.importUsers(file));
    }

    @GetMapping("/import/progress")
    public ApiResult<Object>  getProgress(String taskId) {
        return ApiResult.success(userImportAppService.getProgress(taskId));
    }
}
