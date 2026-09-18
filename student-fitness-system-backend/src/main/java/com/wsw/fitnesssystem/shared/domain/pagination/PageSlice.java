package com.wsw.fitnesssystem.shared.domain.pagination;

import java.util.List;

/**
 * 领域分页切片（读模型专用）。
 *
 * <p><b>职责：</b>承载领域层/仓储层的分页结果，只表达"一页数据 + 总数 + 页码"，
 * 不携带任何传输层语义（无 HTTP、无响应码、无序列化依赖）。</p>
 *
 * <p><b>与 {@code shared.response.PageResult} 的区别：</b></p>
 * <ul>
 *   <li>{@code PageSlice} 属于领域原语，供 domain / repository 使用；</li>
 *   <li>{@code PageResult} 属于响应结构，供应用层对外返回；</li>
 *   <li>翻译职责在 Adapter（Infrastructure），不污染领域层。</li>
 * </ul>
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
        if (items == null) {
            throw new IllegalArgumentException("items must not be null");
        }
        if (pageNum < 1) {
            throw new IllegalArgumentException("pageNum must be >= 1, but was " + pageNum);
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be >= 1, but was " + pageSize);
        }
        if (total < 0) {
            throw new IllegalArgumentException("total must be >= 0, but was " + total);
        }
    }

    /** 当前页是否无数据 */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /** 总页数（向上取整，至少 1 页） */
    public int totalPages() {
        return (int) Math.max(1, (total + pageSize - 1) / pageSize);
    }

    /** 是否为最后一页 */
    public boolean isLast() {
        return pageNum >= totalPages();
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
