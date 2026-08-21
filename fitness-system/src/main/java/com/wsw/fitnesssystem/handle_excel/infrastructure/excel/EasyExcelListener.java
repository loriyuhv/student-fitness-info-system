package com.wsw.fitnesssystem.handle_excel.infrastructure.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * EasyExcel 全量读取监听器
 * 将所有行数据收集到 List 中（适合小文件）
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 15:04
 * @since 1.0
 */
@Slf4j
public class EasyExcelListener<T> extends AnalysisEventListener<T> {
    private final List<T> list;

    public EasyExcelListener(List<T> list) {
        this.list = list;
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        // 过滤空行
        if (data != null) {
            list.add(data);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("Excel 解析完成，共 {} 条数据", list.size());
    }
}
