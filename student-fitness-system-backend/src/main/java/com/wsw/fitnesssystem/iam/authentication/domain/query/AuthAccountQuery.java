package com.wsw.fitnesssystem.iam.authentication.domain.query;

import com.wsw.fitnesssystem.shared.domain.exception.DomainValidationException;
import com.wsw.fitnesssystem.shared.domain.pagination.PageQuery;

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
    int pageNum,
    int pageSize,
    Integer userType,
    Integer status,
    String keyword
) implements PageQuery {

    /**
     * 紧凑构造器：契约不变量校验。
     *
     * <p>正常情况下，{@code pageNum} / {@code pageSize} 由应用层从
     * {@code PageRequest} 的子类翻译而来，已经被上层兜底保护。
     * 此处的校验是<b>领域层最后一道防线</b>：防止应用层误传或未来新增调用方绕过校验。</p>
     *
     * <p>违反不变量时抛 {@link DomainValidationException}，
     * 由应用层统一翻译为 400 / PARAM_INVALID。</p>
     */
    public AuthAccountQuery {
        if (pageNum < 1) {
            throw new DomainValidationException("页码必须大于等于 1，当前值：" + pageNum);
        }
        if (pageSize < 1) {
            throw new DomainValidationException("每页条数必须大于等于 1，当前值：" + pageSize);
        }
    }

}
