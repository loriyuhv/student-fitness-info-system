package com.wsw.fitnesssystem.handle_excel.infrastructure.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * EasyExcel 流式分片监听器
 * 核心优化：不攒全量数据，每攒够 batchSize 就回调处理
 * 内存占用 = batchSize × 单条大小（通常 < 1MB）
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 14:19
 * @since 1.0
 */
@Slf4j
public class StreamBatchListener<T> extends AnalysisEventListener<T> {

    private final List<T> buffer;
    private final Consumer<List<T>> consumer;
    private final int batchSize;
    private int totalCount = 0;
    private int batchCount = 0;

    public StreamBatchListener(Consumer<List<T>> consumer, int batchSize) {
        this.buffer = new ArrayList<>(batchSize);
        this.consumer = consumer;
        this.batchSize = batchSize;
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        if (data == null) return;

        try {
            Method setRowIndex = data.getClass().getMethod("setRowIndex", int.class);
            setRowIndex.invoke(data, context.readRowHolder().getRowIndex() + 1);
        } catch (Exception e) {
            // 忽略
        }

        buffer.add(data);
        totalCount++;

        if (buffer.size() >= batchSize) {
            flush();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!buffer.isEmpty()) {
            flush();
        }
        log.info("Stream parsing completed, total: {} rows, batches: {}", totalCount, batchCount);
    }

    /**
     * 将当前 buffer 交给 consumer 处理，然后清空 buffer
     */
    private void flush() {
        batchCount++;
        // 复制一份交给下游，避免下游修改影响 buffer
        consumer.accept(new ArrayList<>(buffer));
        buffer.clear();
    }
}
