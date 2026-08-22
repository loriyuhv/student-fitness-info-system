package com.wsw.fitnesssystem.handle_excel.core.parser;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.wsw.fitnesssystem.handle_excel.core.exception.ExcelException;
import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ExcelConstants;
import com.wsw.fitnesssystem.handle_excel.infrastructure.excel.EasyExcelListener;
import com.wsw.fitnesssystem.handle_excel.infrastructure.excel.StreamBatchListener;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Excel 通用解析器
 * <p>支持两种模式：</p>
 * <li>1. 全量解析：小文件（小于1w条数据），直接返回 List</li>
 * <li>2. 流式分片解析：大文件（大于1w条数据），边读边处理，内存占用极低</li>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 14:16
 * @since 1.0
 */
@Slf4j
@Component
public class ExcelParser {

    /**
     * 智能解析：根据数据量自动选择全量或流式模式
     * <p>先读取行数预估，小于1w条使用全量解析模式，超过阈值自动切换流式</p>
     * @param file Excel 文件
     * @param dtoClass DTO 类型
     * @return 解析后的数据列表
     * @param <T> DTO 类型
     */
    public <T> List<T> parse(File file, Class<T> dtoClass) {
        long estimatedRows = estimatedRowCount(file);
        if (estimatedRows < ExcelConstants.STREAM_THRESHOLD) {
            log.info("Excel 预估 {} 行，采用全量解析模式", estimatedRows);
            return parseFull(file, dtoClass);
        }

        log.warn("Excel 预估 {} 行，超过 {} 行阈值，自动切换流式解析",
                estimatedRows, ExcelConstants.STREAM_THRESHOLD);
        ArrayList<T> result = new ArrayList<>();
        parseStream(file, dtoClass, ExcelConstants.DEFAULT_BATCH_SIZE, result::addAll);
        return result;
    }

    /**
     * 模式一：全量解析（适合小文件 小于1万条）
     * @param file Excel文件
     * @param dtoClass DTO 类型
     * @return 完整的 List&lt;T&gt;
     * @param <T> DTO类型
     */
    public <T> List<T> parseFull(File file, Class<T> dtoClass) {
        List<T> list = new ArrayList<>();
        try {
            EasyExcel.read(file, dtoClass, new EasyExcelListener<>(list))
                    .sheet().doRead();
        } catch (Exception e) {
            log.error("Excel 解析失败, dtoClass={}, file={}",
                    dtoClass.getSimpleName(), file.getAbsolutePath(), e);
            throw new ExcelException(ResultCode.PARAM_TYPE_ERROR, "Excel 解析失败: " + e.getMessage(), e);
        }
        log.info("Excel 全量解析完成, dtoClass={}, 共 {} 条",
                dtoClass.getSimpleName(), list.size());
        return list;
    }

    /**
     * 模式二：流式分片解析（适合大文件 >= 1万条）
     * <p>每攒够 batchSize 条就回调 consumer，内存里只存当前批次</p>
     *
     * @param file Excel 文件
     * @param dtoClass DTO 类型
     * @param batchSize 每批条数
     * @param consumer 批次处理器（在回调里直接处理，不要长期持有引用）
     * @param <T> DTO类型
     */
    public <T> void parseStream(
            File file, Class<T> dtoClass, int batchSize, Consumer<List<T>> consumer) {

        try {
            EasyExcel.read(file, dtoClass, new StreamBatchListener<>(consumer, batchSize))
                    .sheet().doRead();
        } catch (Exception e) {
            log.error("Excel 流式解析失败, dtoClass={}, file={}",
                    dtoClass.getSimpleName(), file.getAbsolutePath(), e);
            throw new ExcelException(ResultCode.PARAM_TYPE_ERROR, "Excel 解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 快速预估 Excel 数据行数（不含表头，仅读取第一个 sheet）
     * <p>采用无模型流式读取，不反射创建 DTO，性能开销低</p>
     * @param file Excel 文件
     * @return 预估行数；预估失败时返回 0，保守走全量模式
     */
    private long estimatedRowCount(File file) {
        AtomicLong count = new AtomicLong(0);
        try {
            EasyExcel.read(file, new ReadListener<>() {
                @Override
                public void invoke(Object data, AnalysisContext context) {
                    count.incrementAndGet();
                }
                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {}
            }).sheet().doRead();
        } catch (Exception e) {
            log.warn("Excel 行数预估失败，保守按全量模式处理, file={}", file.getAbsolutePath(), e);
            return 0;
        }
        return count.get();
    }

}
