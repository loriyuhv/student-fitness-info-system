package com.wsw.fitnesssystem.user.interfaces.web;

import com.wsw.fitnesssystem.shared.context.RequestContextHolder;
import com.wsw.fitnesssystem.shared.domain.vb.Operator;
import com.wsw.fitnesssystem.shared.response.ApiResult;
import com.wsw.fitnesssystem.shared.response.PageResult;
import com.wsw.fitnesssystem.user.application.dto.query.StudentListQuery;
import com.wsw.fitnesssystem.user.application.dto.result.StudentListItemResult;
import com.wsw.fitnesssystem.user.application.dto.result.UserInfoResult;
import com.wsw.fitnesssystem.user.application.service.query.StudentQueryService;
import com.wsw.fitnesssystem.user.application.service.query.UserInfoQueryService;
import com.wsw.fitnesssystem.user.interfaces.web.dto.UserInfoResponse;
import com.wsw.fitnesssystem.user.interfaces.web.dto.response.StudentListItemResponse;
import com.wsw.fitnesssystem.user.interfaces.web.dto.response.StudentListPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    private final StudentQueryService studentQueryService;
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
            .roles(result.getRoles())
            .permissions(result.getPermissions())
            .build();

        return ApiResult.success(response);
    }

    /**
     * 分页查询学生列表。
     *
     * <p><b>当前阶段：</b>仅管理员可访问，返回全校区学生。</p>
     * <p><b>后续：</b>适配数据权限后，教师/学生访问将自动按范围过滤。</p>
     */
    @GetMapping("/students")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<StudentListPageResponse> listStudents(
        @RequestParam(required = false) Integer pageNum,
        @RequestParam(required = false) Integer pageSize
    ) {

        StudentListQuery query = StudentListQuery.of(pageNum, pageSize);

        PageResult<StudentListItemResult> result =
            studentQueryService.listStudents(query);

        return ApiResult.success(buildResponse(result));
    }

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
