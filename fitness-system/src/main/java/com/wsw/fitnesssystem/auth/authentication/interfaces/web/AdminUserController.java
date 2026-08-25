package com.wsw.fitnesssystem.auth.authentication.interfaces.web;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wsw.fitnesssystem.auth.authentication.application.AuthApplicationService;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import com.wsw.fitnesssystem.shared.response.ApiResult;
import com.wsw.fitnesssystem.shared.response.ResultCode;
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
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<String> kickUser(
        @PathVariable Long campusId, @PathVariable Long userId) {

        Operator operator = new Operator(campusId, userId, null, null);
        Set<String> onlineSessions = authApplicationService.kick(operator);

        String msg;
        if (CollectionUtils.isEmpty(onlineSessions)) {
            return ApiResult.success(ResultCode.SUCCESS, "，该用户当前无在线会话");
        }

        return ApiResult.success(ResultCode.KICKOUT_SUCCESS, "，已踢出" + onlineSessions.size() + "个会话");
    }

}
