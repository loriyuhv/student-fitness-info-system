package com.wsw.fitnesssystem.user.application.dto.query;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 用户列表查询入参（C1）。
 *
 * <p><b>注意：</b>不含 {@code campusId}，校区隔离由数据权限拦截器自动处理。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 18:14
 * @since 1.0
 */
@Getter
@Builder
public class UserListQuery {

    /** 页码（从 1 开始）*/
    private final int pageNum;

    /** 每页条数 */
    private final int pageSize;

    /** 用户类型过滤：0-管理员，1-教师，2-学生；null 表示不过滤 */
    private final Integer userType;

    /** 状态过滤：0-禁用，1-启用；null 表示不过滤 */
    private final Integer status;

    /** 关键词模糊搜索（匹配 username）；空串表示不过滤 */
    private final String keyword;

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    public static UserListQuery of(
        Integer pageNum, Integer pageSize, Integer userType, Integer status, String keyword
    ) {
        int p = (pageNum == null || pageNum < 1) ? DEFAULT_PAGE_NUM : pageNum;
        int s = (pageSize == null || pageSize < 1)
            ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        String k = (StringUtils.isBlank(keyword)) ? null : keyword.trim();
        return UserListQuery.builder()
            .pageNum(p)
            .pageSize(s)
            .userType(userType)
            .status(status)
            .keyword(k)
            .build();
    }

}
