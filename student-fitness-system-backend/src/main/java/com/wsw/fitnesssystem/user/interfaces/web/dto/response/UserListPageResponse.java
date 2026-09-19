package com.wsw.fitnesssystem.user.interfaces.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 用户列表分页响应（Web 出参）。
 *
 * <p>只承载数据，不包含任何转换逻辑，转换由 {@code UserListWebAssembler} 负责。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 18:25
 * @since 1.0
 */
public record UserListPageResponse(
    long total,
    @JsonProperty("page_num") int pageNum,
    @JsonProperty("page_size") int pageSize,
    List<UserListItemResponse> items
) {
}
