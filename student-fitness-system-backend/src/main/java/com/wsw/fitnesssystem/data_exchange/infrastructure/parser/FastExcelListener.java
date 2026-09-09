package com.wsw.fitnesssystem.data_exchange.infrastructure.parser;

import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AnalysisEventListener;

import java.util.List;

/**
 * FastExcel 全量解析监听器。
 * <p>
 * 将所有行数据收集到 List 中，供 {@link ExcelFileParser#parseFull} 使用。
 * 适合小文件场景，大文件请使用 {@link StreamBatchListener}。
 * </p>
 *
 * @param <T> DTO 类型
 * @author loriyuhv
 * @version 1.0 2026/8/21 15:04
 * @since 1.0
 */
public class FastExcelListener<T> extends AnalysisEventListener<T> {

    private final List<T> list;

    public FastExcelListener(List<T> list) {
        this.list = list;
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        // 1. 空行跳过
        if (data == null) return;

        // 2. 设置行号（如果 DTO 实现了 RowIndexAware 接口）
        Integer rowNum = context.readRowHolder().getRowIndex() + 1;

        if (data instanceof RowIndexAware aware) {
            aware.setRowIndex(rowNum);
        } // 无接口则静默跳过

        // 3. 添加解析后的数据
        list.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 无需额外操作
    }

}
