package com.wsw.fitnesssystem.user.application.dto.query;

import lombok.Builder;
import lombok.Getter;

/**
 * 学生列表查询入参（应用层）。
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 18:14
 * @since 1.0
 */
@Getter
@Builder
public class StudentListQuery {

    /** 页码（从 1 开始）*/
    private final int pageNum;

    /** 每页条数 */
    private final int pageSize;

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    public static StudentListQuery of(Integer pageNum, Integer pageSize) {
        int p = (pageNum == null || pageNum < 1) ? DEFAULT_PAGE_NUM : pageNum;
        int s = (pageSize == null || pageSize < 1)
            ? DEFAULT_PAGE_SIZE
            : Math.min(pageSize, MAX_PAGE_SIZE);
        return StudentListQuery.builder().pageNum(p).pageSize(s).build();
    }

}
