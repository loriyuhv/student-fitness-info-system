package com.wsw.fitnesssystem.user.interfaces.web;

import com.wsw.fitnesssystem.shared.context.RequestContextHolder;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import com.wsw.fitnesssystem.shared.response.ApiResult;
import com.wsw.fitnesssystem.user.application.dto.result.UserInfoResult;
import com.wsw.fitnesssystem.user.application.service.impl.UserInfoQueryService;
import com.wsw.fitnesssystem.user.interfaces.web.dto.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息控制器
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>处理当前登录用户的信息查询请求</li>
 *   <li>负责 Web 层协议适配：获取 Operator → 调用 Application 服务 → 转换为 Web Response</li>
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
     * 获取当前登录用户个人信息
     *
     * @return 用户信息响应
     */
    @GetMapping("/info")
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
            .permissions(result.getPermissions())
            .build();

        return ApiResult.success(response);
    }

}
