package com.wsw.fitnesssystem.user.interfaces.web.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wsw.fitnesssystem.shared.interfaces.web.pagination.PageRequest;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户列表查询请求（Web 入参）。
 *
 * <p>分页字段从 {@link PageRequest} 继承，只定义业务筛选字段。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/19 11:25
 * @since 1.0
 */
@Getter
@Setter
public class UserListRequest extends PageRequest {

    /**
     * 用户类型过滤：null 表示不过滤
     */
    @JsonProperty("user_type")
    private Integer userType;

    /**
     * 状态过滤：null 表示不过滤
     */
    private Integer status;

    /**
     * 关键词模糊搜索；空串表示不过滤
     */
    private String keyword;

}
