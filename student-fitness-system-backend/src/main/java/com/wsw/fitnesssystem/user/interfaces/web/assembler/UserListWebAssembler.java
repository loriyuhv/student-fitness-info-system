package com.wsw.fitnesssystem.user.interfaces.web.assembler;

import com.wsw.fitnesssystem.shared.domain.pagination.PageSlice;
import com.wsw.fitnesssystem.user.application.dto.query.UserListQuery;
import com.wsw.fitnesssystem.user.application.dto.result.UserListItemResult;
import com.wsw.fitnesssystem.user.interfaces.web.dto.request.UserListRequest;
import com.wsw.fitnesssystem.user.interfaces.web.dto.response.UserListItemResponse;
import com.wsw.fitnesssystem.user.interfaces.web.dto.response.UserListPageResponse;

import java.util.List;

/**
 * 用户列表 Web 适配器。
 *
 * <p><b>职责：</b></p>
 * <ul>
 *   <li>{@code UserListRequest}  → {@code UserListQuery}</li>
 *   <li>{@code UserListItemResult} → {@code UserListItemResponse}</li>
 *   <li>{@code PageSlice<UserListItemResult>} → {@code UserListPageResponse}</li>
 * </ul>
 *
 * <p>只做结构与语义映射，不写业务规则。Application 层不得反向依赖本类。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/19 11:44
 * @since 1.0
 */
public final class UserListWebAssembler {

    private UserListWebAssembler() {}

    public static UserListQuery toQuery(UserListRequest request) {
        return new UserListQuery(
            request.pageNum(),
            request.pageSize(),
            request.getUserType(),
            request.getStatus(),
            request.getKeyword()
        );
    }

    /**
     * {@code PageSlice<UserListItemResult>} → {@code UserListPageResponse}。
     * <p>分页字段通过 {@link PageSlice} 的访问器读取，业务字段通过 {@code getXxx()} 读取。</p>
     */
    public static UserListPageResponse toResponse(PageSlice<UserListItemResult> page) {
        List<UserListItemResponse> items = page.items().stream()
            .map(UserListWebAssembler::toItemResponse).toList();

        return new UserListPageResponse(
            page.total(),
            page.pageNum(),
            page.pageSize(),
            items
        );
    }

    private static UserListItemResponse toItemResponse(UserListItemResult r) {
        return new UserListItemResponse(
            r.getUserId(),
            r.getCampusId(),
            r.getUsername(),
            r.getUserType(),
            r.getStatus(),
            r.getNickname(),
            r.getPhoneNumber(),
            r.getEmail(),
            r.getStudentNo(),
            r.getClassId(),
            r.getMajor(),
            r.getTeacherNo()
        );
    }

}
