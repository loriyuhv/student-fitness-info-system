package com.wsw.fitnesssystem.iam.authentication.interfaces.web.controller;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wsw.fitnesssystem.iam.authentication.application.AuthAppService;
import com.wsw.fitnesssystem.iam.error.IamAuthNErrorCode;
import com.wsw.fitnesssystem.shared.interfaces.web.response.ApiResponse;
import com.wsw.fitnesssystem.shared.kernel.error.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 16:00
 * @since 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/auth/users")
public class AdminAuthUserController {

    private final AuthAppService authAppService;

    /**
     * 踢掉用户所有在线会话（管理员操作）
     *
     * @param campusId 校区ID
     * @param userId 用户ID
     * @return 成功返回操作结果
     */
    @PostMapping("/{campusId}/{userId}/kick")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> kickUser(
        @PathVariable Long campusId, @PathVariable Long userId) {

        Set<String> onlineSessions = authAppService.kick(campusId, userId);

        String msg;
        if (CollectionUtils.isEmpty(onlineSessions)) {
            msg = CommonErrorCode.SUCCESS.message() + "，该用户当前无在线会话";
            return ApiResponse.success(msg);
        }

        msg = IamAuthNErrorCode.KICKOUT_SUCCESS.message() + "，已踢出" + onlineSessions.size() + "个会话";
        return ApiResponse.success(msg);
    }

}
