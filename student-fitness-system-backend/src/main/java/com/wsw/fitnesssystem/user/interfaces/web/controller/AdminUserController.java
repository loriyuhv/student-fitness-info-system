package com.wsw.fitnesssystem.user.interfaces.web.controller;

import com.wsw.fitnesssystem.shared.response.ApiResult;
import com.wsw.fitnesssystem.shared.response.PageResult;
import com.wsw.fitnesssystem.user.application.dto.query.StudentListQuery;
import com.wsw.fitnesssystem.user.application.dto.result.AdminUserDetailResult;
import com.wsw.fitnesssystem.user.application.dto.result.StudentListItemResult;
import com.wsw.fitnesssystem.user.application.service.query.StudentQueryService;
import com.wsw.fitnesssystem.user.application.service.query.UserInfoQueryService;
import com.wsw.fitnesssystem.user.interfaces.web.dto.response.AdminUserDetailResponse;
import com.wsw.fitnesssystem.user.interfaces.web.dto.response.StudentListItemResponse;
import com.wsw.fitnesssystem.user.interfaces.web.dto.response.StudentListPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    private final StudentQueryService studentQueryService;
    private final UserInfoQueryService userInfoQueryService;

    /**
     * C1：分页查询用户列表。
     *
     * <p><b>功能权限：</b>{@code system:user:view}。教师、学生无权访问。</p>
     * <p><b>数据范围：</b>校区管理员 → 本校区；超级管理员 → 全部。</p>
     *
     * <p><b>当前阶段：</b>列表仅返回学生（由 {@code StudentQueryService} 提供）。
     * 后续扩展为"支持按 userType 过滤"。</p>
     */
    @GetMapping
    @PreAuthorize("hasAuthority('system:user:view')")
    public ApiResult<StudentListPageResponse> listUsers(
        @RequestParam(required = false) Integer pageNum,
        @RequestParam(required = false) Integer pageSize
    ) {
        StudentListQuery query = StudentListQuery.of(pageNum, pageSize);

        PageResult<StudentListItemResult> result =
            studentQueryService.listStudents(query);

        return ApiResult.success(buildResponse(result));
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
    public ApiResult<AdminUserDetailResponse> getUserDetail(@PathVariable Long userId) {
        AdminUserDetailResult result = userInfoQueryService.getUserDetailForAdmin(userId);
        return ApiResult.success(AdminUserDetailResponse.from(result));
    }

    // ==================== 响应转换 ====================

    private StudentListPageResponse buildResponse(PageResult<StudentListItemResult> result) {
        List<StudentListItemResponse> items = result.getItems().stream()
            .map(this::buildItemResponse)
            .toList();

        return StudentListPageResponse.builder()
            .total(result.getTotal())
            .pageNum(result.getPageNum())
            .pageSize(result.getPageSize())
            .items(items)
            .build();
    }

    private StudentListItemResponse buildItemResponse(StudentListItemResult r) {
        return StudentListItemResponse.builder()
            .studentId(r.getStudentId())
            .userId(r.getUserId())
            .studentNo(r.getStudentNo())
            .classId(r.getClassId())
            .enrollYear(r.getEnrollYear())
            .major(r.getMajor())
            .gender(r.getGender())
            .familyAddress(r.getFamilyAddress())
            .nickname(r.getNickname())
            .phoneNumber(r.getPhoneNumber())
            .email(r.getEmail())
            .build();
    }

}
