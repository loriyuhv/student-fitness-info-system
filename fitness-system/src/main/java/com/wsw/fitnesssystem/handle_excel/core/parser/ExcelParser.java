package com.wsw.fitnesssystem.handle_excel.core.parser;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.wsw.fitnesssystem.handle_excel.core.exception.ExcelException;
import com.wsw.fitnesssystem.handle_excel.core.exception.ImportCancelledException;
import com.wsw.fitnesssystem.handle_excel.infrastructure.excel.EasyExcelListener;
import com.wsw.fitnesssystem.handle_excel.infrastructure.excel.StreamBatchListener;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Excel 通用解析器
 * <p>支持两种模式：</p>
 * <li>1. 全量解析：小文件（小于1w条数据），直接返回 List</li>
 * <li>2. 流式分片解析：大文件（大于1w条数据），边读边处理，内存占用极低</li>
 * <p><b>重要：调用方必须根据场景显式选择 parseFull 或 parseStream，</b></p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 14:16
 * @since 1.0
 */
@Slf4j
@Component
public class ExcelParser {

    /**
     * 模式一：全量解析（适合小文件 小于1万条）
     * <p>内存占用 = 全量数据，仅在小文件场景使用</p>
     *
     * @param file Excel文件
     * @param dtoClass DTO 类型
     * @return 完整的 List&lt;T&gt;
     * @param <T> DTO类型
     */
    public <T> List<T> parseFull(File file, Class<T> dtoClass, String taskId) {
        List<T> list = new ArrayList<>();

        try {
            EasyExcel.read(file, dtoClass, new EasyExcelListener<>(list)).sheet().doRead();
        } catch (ImportCancelledException e) {
            // 取消异常直接原样抛出
            throw e;
        } catch (Exception e) {
            log.error("Excel full parse failed, dtoClass={}, file={}",
                dtoClass.getSimpleName(), file.getAbsolutePath(), e);
            throw new ExcelException(
                ResultCode.PARAM_TYPE_ERROR, "Excel parse failed: " + e.getMessage(), e);
        }

        log.info("[{}] Excel full parse completed, dtoClass={}, total={} rows",
            taskId, dtoClass.getSimpleName(), list.size());

        return list;
    }

    /**
     * 模式二：流式分片解析（适合大文件 >= 1万条）
     * <p>每攒够 batchSize 条就回调 consumer，内存里只存当前批次</p>
     * <p><b>注意：consumer 执行完一批后，该批数据即可被 GC，严禁在 consumer 中长期持有引用。</b></p>
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
        } catch (ImportCancelledException e) {
            // 取消异常直接原样抛出，不包装
            throw e;
        } catch (Exception e) {
            log.error("Excel stream parse failed, dtoClass={}, file={}",
                dtoClass.getSimpleName(), file.getAbsolutePath(), e);
            throw new ExcelException(ResultCode.PARAM_TYPE_ERROR, "Excel parse failed: " + e.getMessage(), e);
        }
    }

    /**
     * 快速预估 Excel 数据行数（不含表头，仅读取第一个 sheet）
     * <p>采用无模型流式读取，不反射创建 DTO，性能开销低</p>
     * <p>上层根据返回值决定走全量还是流式模式</p>
     *
     * @param file Excel 文件
     * @return 预估行数；预估失败时返回 0，保守走全量模式
     */
    public int estimatedRowCount(File file) {
        AtomicInteger count = new AtomicInteger(0);

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
            log.warn("Row count estimation failed, falling back to full processing mode, file={}",
                file.getAbsolutePath(), e);
            return 0;
        }

        return count.get();
    }

}
