package com.wsw.fitnesssystem.user.application.dto.query;

/**
 * 用户列表分页查询（应用层 DTO）。
 *
 * <p><b>不继承任何 Web 基类</b>，纯 record。
 * 分页字段由接口层 {@code UserListRequest} 翻译而来，
 * 已通过 {@code PageRequest} 的 {@code @Min} / {@code @Max} 校验。</p>
 *
 * <p><b>注意：</b>不含 {@code campusId}，校区隔离由数据权限拦截器自动处理。</p>
 *
 * @param pageNum  页码（从 1 开始）
 * @param pageSize 每页条数
 * @param userType 用户类型过滤：0-管理员，1-教师，2-学生；null 表示不过滤
 * @param status   状态过滤：0-禁用，1-启用；null 表示不过滤
 * @param keyword  关键词模糊搜索（匹配 username）；空串表示不过滤
 * @author loriyuhv
 * @version 1.0 2026/9/14 18:14
 * @since 1.0
 */
public record UserListQuery (
    int pageNum,
    int pageSize,
    Integer userType,
    Integer status,
    String keyword
) {
}
