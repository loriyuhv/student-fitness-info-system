package com.wsw.fitnesssystem.user.interfaces.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class StudentListPageResponse {

    private long total;

    @JsonProperty("page_num")
    private int pageNum;

    @JsonProperty("page_size")
    private int pageSize;

    private List<StudentListItemResponse> items;

}
