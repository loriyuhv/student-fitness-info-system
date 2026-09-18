package com.wsw.fitnesssystem.user.interfaces.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wsw.fitnesssystem.shared.response.PageResult;
import com.wsw.fitnesssystem.user.application.dto.result.UserListItemResult;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/14 18:25
 * @since 1.0
 */
@Data
@Builder
public class UserListPageResponse {

    private long total;

    @JsonProperty("page_num")
    private int pageNum;

    @JsonProperty("page_size")
    private int pageSize;

    private List<UserListItemResponse> items;

    public static UserListPageResponse from(PageResult<UserListItemResult> result) {
        List<UserListItemResponse> items = result.getItems().stream()
            .map(UserListItemResponse::from)
            .toList();
        return UserListPageResponse.builder()
            .total(result.getTotal())
            .pageNum(result.getPageNum())
            .pageSize(result.getPageSize())
            .items(items)
            .build();
    }

}
