package com.wsw.fitnesssystem.shared.domain.pagination;

import com.wsw.fitnesssystem.shared.domain.exception.DomainValidationException;

import java.util.List;

/**
 * 领域分页切片（通用分页结果容器）。
 *
 * <p><b>职责：</b>承载分页查询的通用结果，只表达"一页数据 + 总数 + 页码"，
 * 不携带任何传输层语义（无 HTTP、无响应码、无序列化依赖）。</p>
 *
 * <p><b>使用范围：</b>作为分页结果原语，被 domain / application / interfaces
 * 三层共享。全链路统一使用本类，不再为各层重复定义字段相同的分页结果类。</p>
 *
 * <p><b>不变量：</b></p>
 * <ul>
 *   <li>{@code items} 永不为 null（无数据时为空列表）；</li>
 *   <li>{@code pageNum}、{@code pageSize} 恒为正整数；</li>
 *   <li>{@code total} 为跨页累计记录数，可能为 0。</li>
 * </ul>
 *
 * @param <T> 元素类型
 * @author loriyuhv
 * @version 1.0 2026/9/19 06:28
 * @since 1.0
 */
public record PageSlice<T>(
    List<T> items,
    long total,
    int pageNum,
    int pageSize
) {

    /**
     * 紧凑构造器：领域不变量校验。
     * <p>任何破坏"分页结构完整性"的参数都在此处暴露，
     * 避免非法分页对象在系统内流转。</p>
     */
    public PageSlice {
        // 校验集合不能为null
        if (items == null) {
            throw new DomainValidationException("items不能为null");
        }
        // 页码必须大于等于1
        if (pageNum < 1) {
            throw new DomainValidationException("页码必须 >= 1，当前值：" + pageNum);
        }
        // 每页条数必须大于等于1
        if (pageSize < 1) {
            throw new DomainValidationException("每页条数必须 >= 1，当前值：" + pageSize);
        }
        // 总记录数不能为负数
        if (total < 0) {
            throw new DomainValidationException("总记录数必须 >= 0，当前值：" + total);
        }
    }

    /** 当前页是否无数据 */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    // ==================== 工厂方法 ====================

    /** 空分页 */
    public static <T> PageSlice<T> empty(int pageNum, int pageSize) {
        return new PageSlice<>(List.of(), 0L, pageNum, pageSize);
    }

    /** 构造分页切片 */
    public static <T> PageSlice<T> of(List<T> items, long total, int pageNum, int pageSize) {
        return new PageSlice<>(items, total, pageNum, pageSize);
    }

}
