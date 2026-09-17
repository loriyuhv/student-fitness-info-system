package com.wsw.fitnesssystem.user.interfaces.web.controller;

import com.wsw.fitnesssystem.shared.context.RequestContextHolder;
import com.wsw.fitnesssystem.shared.domain.vb.Operator;
import com.wsw.fitnesssystem.shared.response.ApiResult;
import com.wsw.fitnesssystem.user.application.dto.result.UserInfoResult;
import com.wsw.fitnesssystem.user.application.service.query.UserInfoQueryService;
import com.wsw.fitnesssystem.user.interfaces.web.dto.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人中心控制器。
 *
 * <p><b>职责：</b>当前登录用户对自己信息的查询与修改。</p>
 * <p><b>权限策略：</b>所有登录用户均可访问，数据范围由 {@code operator.userId()} 强约束为"仅自己"。</p>
 * <p>负责 Web 层协议适配：获取 Operator → 调用 Application 服务 → 转换为 Web Response</p>
 *
 * <p><b>接口清单（A 类）：</b></p>
 * <ul>
 *   <li>A1 {@code GET  /user/me}           - 查询自己的完整信息</li>
 *   <li>A2 {@code PUT  /user/me}           - 修改自己的画像（待实现）</li>
 *   <li>A3 {@code PUT  /user/me/password}  - 修改自己的密码（待实现）</li>
 *   <li>A4 {@code POST /user/me/avatar}    - 上传头像（待定）</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 15:59
 * @since 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserInfoQueryService userInfoQueryService;

    /**
     * A1：获取当前登录用户个人信息。
     *
     * <p><b>数据范围：</b>仅自己（通过 {@code operator.userId()} 强约束）。</p>
     * <p><b>返回字段：</b>聚合 sys_user + user_profile + 角色 + 权限；<b>不返回</b> id_card。</p>
     *
     * @return 用户信息响应
     */
    @GetMapping("/me")
    public ApiResult<UserInfoResponse> getCurrentUserInfo() {
        // 1. 从安全上下文中获取当前操作者
        Operator operator = RequestContextHolder.getRequiredOperator();

        // 2. 调用应用层查询服务
        UserInfoResult result = userInfoQueryService.getCurrentUserInfo(operator);

        // 3. 防腐层转换：UserInfoResult → UserInfoResponse
        UserInfoResponse response = UserInfoResponse.builder()
            .userId(result.getUserId())
            .campusId(result.getCampusId())
            .username(result.getUsername())
            .nickname(result.getNickname())
            .phoneNumber(result.getPhoneNumber())
            .email(result.getEmail())
            .remark(result.getRemark())
            .userType(result.getUserType())
            .roles(result.getRoles())
            .permissions(result.getPermissions())
            .build();

        return ApiResult.success(response);
    }

}
