package com.wsw.fitnesssystem.iam.authentication.domain.query;

/**
 * 账号分页查询条件（iam 领域层自有模型）。
 *
 * <p><b>设计动机：</b>iam 领域仓储不应依赖其他模块的应用层 DTO，
 * 因此定义本模块自有的查询对象，由 Adapter 负责从外部 DTO 翻译。</p>
 *
 * <p><b>字段语义：</b></p>
 * <ul>
 *   <li>{@code pageNum} / {@code pageSize}：分页参数，必填且为正；</li>
 *   <li>{@code userType} / {@code status}：可选筛选，null 表示不参与过滤；</li>
 *   <li>{@code keyword}：可选模糊匹配（username），null/空串表示不参与过滤。</li>
 * </ul>
 *
 * <p><b>注意：</b>本对象不含 {@code campusId}。
 * 校区过滤由数据权限拦截器自动追加，不通过查询对象传递。</p>
 *
 * @param pageNum  页码（从 1 开始）
 * @param pageSize 每页条数
 * @param userType 用户类型（0-管理员，1-教师，2-学生；null 表示全部）
 * @param status   业务状态（0-禁用，1-启用；null 表示全部）
 * @param keyword  用户名模糊匹配关键字（可空）
 * @author loriyuhv
 * @version 1.0 2026/9/19 06:15
 * @since 1.0
 */
public record AuthAccountQuery(
    Integer pageNum,
    Integer pageSize,
    Integer userType,
    Integer status,
    String keyword
) {

    public AuthAccountQuery {
        if (pageNum < 1) {
            throw new IllegalArgumentException("pageNum must be >= 1");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be >= 1");
        }
    }

}
