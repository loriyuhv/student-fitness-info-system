package com.wsw.fitnesssystem.shared.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 通用分页结果。
 *
 * <p><b>职责：</b>承载分页查询的通用返回结构，供跨层使用。</p>
 * <p><b>设计原则：</b>不含任何框架依赖（MyBatis-Plus Page 等），保持纯净。</p>
 *
 * @param <T> 列表元素类型
 * @author loriyuhv
 * @version 1.0 2026/9/14 18:12
 * @since 1.0
 */
@Getter
@Builder
public class PageResult<T> {

    /** 总记录数（跨页累计）*/
    private final long total;

    /** 当前页码（从 1 开始）*/
    private final int pageNum;

    /** 每页条数 */
    private final int pageSize;

    /** 当前页数据 */
    private final List<T> items;

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public static <T> PageResult<T> empty(int pageNum, int pageSize) {
        return PageResult.<T>builder()
            .total(0)
            .pageNum(pageNum)
            .pageSize(pageSize)
            .items(List.of())
            .build();
    }

    public static <T> PageResult<T> of(List<T> items, long total, int pageNum, int pageSize) {
        return PageResult.<T>builder()
            .total(total)
            .pageNum(pageNum)
            .pageSize(pageSize)
            .items(items)
            .build();
    }

}
