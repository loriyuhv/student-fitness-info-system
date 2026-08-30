package com.wsw.fitnesssystem.handle_excel.infrastructure.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
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
        // 1. 空行直接返回
        if (data == null) return;

        // 2. 通过反射设置行号
        Integer rowNum = context.readRowHolder().getRowIndex() + 1;

        try {
            Method setRowIndex = data.getClass().getMethod("setRowIndex", Integer.class);
            setRowIndex.invoke(data,  rowNum);
        } catch (NoSuchMethodException ignored) {
            // 当前DTO不需要注入行号，属于正常场景，静默跳过
        } catch (Exception e) {
            log.error("为Excel DTO设置行号失败，row:{}", rowNum, e);
        }

        // 3. 添加解析后的数据
        list.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("Excel 解析完成，共 {} 条数据", list.size());
    }
}
