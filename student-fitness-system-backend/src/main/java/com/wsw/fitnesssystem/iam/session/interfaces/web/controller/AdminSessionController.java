package com.wsw.fitnesssystem.iam.session.interfaces.web.controller;

import com.wsw.fitnesssystem.iam.session.application.dto.command.RevokeSessionCommand;
import com.wsw.fitnesssystem.iam.session.application.dto.result.RevokeSessionResult;
import com.wsw.fitnesssystem.iam.session.application.service.command.RevokeSessionCommandService;
import com.wsw.fitnesssystem.iam.session.interfaces.web.dto.assembler.RevokeSessionWebAssembler;
import com.wsw.fitnesssystem.iam.session.interfaces.web.dto.response.RevokeSessionResponse;
import com.wsw.fitnesssystem.shared.interfaces.web.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端会话控制器
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>面向管理员的会话管理接口</li>
 *   <li>目前仅提供"撤销用户全部会话"能力，后续可扩展"查询在线会话列表"、"单设备下线"等</li>
 * </ul>
 *
 * <p><b>权限模型：</b>所有接口要求 ADMIN 角色。</p>
 *
 * <p><b>ACL 规范：</b>
 * <ul>
 *   <li>Controller 不做业务逻辑，只做协议适配</li>
 *   <li>Web 数据 → Command 由 {@link RevokeSessionWebAssembler} 完成</li>
 *   <li>Result → Response 由 {@link RevokeSessionWebAssembler} 完成</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21 15:34
 * @since 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/sessions")
public class AdminSessionController {

    private final RevokeSessionWebAssembler revokeSessionWebAssembler;
    private final RevokeSessionCommandService revokeSessionCommandService;

    /**
     * 撤销指定用户的所有在线会话
     *
     * <p>典型场景：管理员强制用户下线、账号安全事件处置、权限变更后强制重登。</p>
     *
     * @param campusId 校区ID
     * @param userId   目标用户ID
     * @return 撤销结果（被撤销的会话数量与 Token 列表）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/campus/{campus_id}/users/{user_id}")
    public ApiResponse<RevokeSessionResponse> revokeUserSessions(
        @PathVariable("campus_id") Long campusId,
        @PathVariable("user_id") Long userId
    ) {
        // 1. Web 数据 → Application 层 Command
        RevokeSessionCommand command = revokeSessionWebAssembler.toCommand(campusId, userId);

        // 2. 调用 Application 层
        RevokeSessionResult result = revokeSessionCommandService.revoke(command);

        // 3. Result → Response（协议适配）
        RevokeSessionResponse response = revokeSessionWebAssembler.toResponse(result);

        return ApiResponse.success(response);
    }

}
