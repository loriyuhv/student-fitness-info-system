package com.wsw.fitnesssystem.auth.interfaces.web;

import com.wsw.fitnesssystem.auth.application.authentication.AuthApplicationService;
import com.wsw.fitnesssystem.shared.response.ApiResult;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 16:00
 * @since 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {
    private final AuthApplicationService authApplicationService;

    /**
     * 踢掉用户所有在线会话（管理员操作）
     *
     * @param campusId 校区ID
     * @param userId 用户ID
     * @return 成功返回操作结果
     */
    @PostMapping("/{campusId}/{userId}/kick")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 只允许管理员访问
    public ApiResult<String> kickUser(
        @PathVariable Long campusId,
        @PathVariable Long userId
    ) {
        try {
            authApplicationService.kick(campusId, userId);
            return ApiResult.success(ResultCode.KICKOUT_SUCCESS.getMessage(), "操作完成");
        } catch (Exception e) {
            log.error("踢人失败，campusId={} userId={}", campusId, userId, e);
            return ApiResult.from(ResultCode.KICKOUT_FAILED, e.getMessage());
        }
    }

}
