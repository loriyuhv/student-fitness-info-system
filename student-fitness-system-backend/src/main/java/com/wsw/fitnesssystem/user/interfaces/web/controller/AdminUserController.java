package com.wsw.fitnesssystem.user.interfaces.web.controller;

import com.wsw.fitnesssystem.shared.domain.pagination.PageSlice;
import com.wsw.fitnesssystem.shared.interfaces.web.response.ApiResponse;
import com.wsw.fitnesssystem.user.application.dto.query.UserListQuery;
import com.wsw.fitnesssystem.user.application.dto.result.AdminUserDetailResult;
import com.wsw.fitnesssystem.user.application.dto.result.UserListItemResult;
import com.wsw.fitnesssystem.user.application.service.query.UserQueryService;
import com.wsw.fitnesssystem.user.application.service.query.UserInfoQueryService;
import com.wsw.fitnesssystem.user.interfaces.web.assembler.UserListWebAssembler;
import com.wsw.fitnesssystem.user.interfaces.web.dto.request.UserListRequest;
import com.wsw.fitnesssystem.user.interfaces.web.dto.response.AdminUserDetailResponse;
import com.wsw.fitnesssystem.user.interfaces.web.dto.response.UserListPageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器（管理员专属）。
 *
 * <p><b>职责：</b>管理员对用户的查询与变更操作。</p>
 * <p><b>权限策略：</b>所有接口必须拥有 {@code system:user:*} 权限。教师、学生一律被功能权限拦截，不进入数据权限层。</p>
 * <p><b>数据范围：</b>由数据权限拦截器自动收敛（COLLEGE → 本校区；ALL → 全部）。</p>
 *
 * <p><b>接口清单（C 类）：</b></p>
 * <ul>
 *   <li>C1 {@code GET    /admin/users}                            - 分页查询用户列表</li>
 *   <li>C2 {@code GET    /admin/users/{userId}}                   - 用户详情（待实现）</li>
 *   <li>C3 {@code PUT    /admin/users/{userId}/profile}           - 修改画像（待实现）</li>
 *   <li>C4 {@code PUT    /admin/users/{userId}/student-profile}   - 修改学生扩展（待实现）</li>
 *   <li>C4 {@code PUT    /admin/users/{userId}/teacher-profile}   - 修改教师扩展（待实现）</li>
 *   <li>C5 {@code PUT    /admin/users/{userId}/status}            - 启用/禁用（待实现）</li>
 *   <li>C6 {@code DELETE /admin/users/{userId}}                   - 逻辑删除（待实现，仅超管）</li>
 *   <li>C8 {@code PUT    /admin/users/{userId}/password/reset}    - 重置密码（待实现）</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/18 06:09
 * @since 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserQueryService userQueryService;
    private final UserInfoQueryService userInfoQueryService;

    /**
     * C1：分页查询用户列表。
     *
     * <p><b>功能权限：</b>{@code system:user:view}。教师、学生无权访问。</p>
     * <p><b>数据范围：</b>校区管理员 → 本校区；超级管理员 → 全部。</p>
     *
     * <p><b>请求方式：</b>POST + JSON Body，便于复杂筛选条件与蛇形字段统一。</p>
     */
    @PostMapping("/query")
    @PreAuthorize("hasAuthority('system:user:view')")
    public ApiResponse<UserListPageResponse> listUsers(@Valid @RequestBody UserListRequest request) {
        UserListQuery query = UserListWebAssembler.toQuery(request);

        PageSlice<UserListItemResult> result = userQueryService.listUsers(query);

        UserListPageResponse response = UserListWebAssembler.toResponse(result);

        return ApiResponse.success(response);
    }

    /**
     * C2：查询用户详情。
     *
     * <p><b>功能权限：</b>{@code system:user:view}。</p>
     * <p><b>数据范围：</b>校区管理员 → 本校区；超管 → 全部。</p>
     * <p><b>脱敏：</b>id_card 返回时已脱敏。</p>
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('system:user:view')")
    public ApiResponse<AdminUserDetailResponse> getUserDetail(@PathVariable Long userId) {
        AdminUserDetailResult result = userInfoQueryService.getUserDetailForAdmin(userId);
        return ApiResponse.success(AdminUserDetailResponse.from(result));
    }

}
