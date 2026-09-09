package com.wsw.fitnesssystem.data_exchange.infrastructure.parser;

import cn.idev.excel.FastExcel;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.read.listener.ReadListener;
import com.wsw.fitnesssystem.data_exchange.infrastructure.exception.ImportInfrastructureException;
import com.wsw.fitnesssystem.data_exchange.domain.exception.ImportCancelledException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Excel/CSV 文件解析器。
 * <p>
 * 基于 FastExcel 实现，支持两种解析模式：
 * <ul>
 *   <li>全量解析（{@link #parseFull}）：适合小文件（&lt; 1万行），一次性返回完整 List</li>
 *   <li>流式解析（{@link #parseStream}）：适合大文件（≥ 1万行），边读边处理，内存占用 O(batchSize)</li>
 * </ul>
 * <p><b>重要：调用方必须根据场景显式选择 parseFull 或 parseStream，</b></p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 14:16
 * @since 1.0
 */
@Slf4j
@Component
public class ExcelFileParser {

    /**
     * 全量解析（适合小文件 &lt; 1万行）。
     * <p>一次性解析全部数据到内存</p>
     * <b>注意：</b>
     * <p>仅在小文件场景使用，大文件请使用 {@link #parseStream}。</p>
     *
     * @param file     待解析的文件
     * @param dtoClass 目标 DTO 类型（由 FastExcel 反射映射）
     * @param taskId   任务 ID（用于日志跟踪）
     * @param <T>      DTO 类型
     * @return 完整的 DTO 列表
     * @throws ImportInfrastructureException 解析失败时抛出
     */
    public <T> List<T> parseFull(File file, Class<T> dtoClass, String taskId) {
        List<T> list = new ArrayList<>();

        try {
            FastExcel.read(file, dtoClass, new FastExcelListener<>(list)).sheet().doRead();
        } catch (ImportCancelledException e) {
            // 取消异常直接抛出，由上层处理
            throw e;
        } catch (Exception e) {
            log.error("Excel full parse failed, dtoClass={}, file={}",
                dtoClass.getSimpleName(), file.getAbsolutePath(), e);
            throw new ImportInfrastructureException(
                ResultCode.PARAM_TYPE_ERROR, "文件解析失败：" + e.getMessage(), e
            );
        }

        log.info("[{}] Excel full parse completed, dtoClass={}, total={} rows",
            taskId, dtoClass.getSimpleName(), list.size());

        return list;
    }

    /**
     * 流式分片解析（适合大文件 ≥ 1万行）。
     * <p>
     * 每攒够 batchSize 条数据触发一次回调，内存中只保留当前批次。
     * <b>重要：consumer 执行完后该批数据即可被 GC，严禁在 consumer 中长期持有引用。</b>
     * </p>
     *
     * @param file      待解析的文件
     * @param dtoClass  目标 DTO 类型
     * @param batchSize 每批处理条数
     * @param consumer  批次处理器（在回调中直接处理，不要长期持有引用）
     * @param <T>       DTO 类型
     * @throws ImportInfrastructureException 解析失败时抛出
     */
    public <T> void parseStream(
            File file, Class<T> dtoClass, int batchSize, Consumer<List<T>> consumer) {

        try {
            FastExcel.read(
                file, dtoClass, new StreamBatchListener<>(consumer, batchSize)
            ).sheet().doRead();
        } catch (ImportCancelledException e) {
            throw e;
        } catch (Exception e) {
            log.error("Excel stream parse failed, dtoClass={}, file={}",
                dtoClass.getSimpleName(), file.getAbsolutePath(), e
            );
            throw new ImportInfrastructureException(
                ResultCode.PARAM_TYPE_ERROR, "文件解析失败：" + e.getMessage(), e
            );
        }
    }

    /**
     * 快速预估文件行数（不含表头）。
     * <p>采用无模型流式读取，不反射创建 DTO，性能开销极低</p>
     * <p>调用方根据返回值决策使用全量还是流式模式。</p>
     *
     * @param file 待预估的文件
     * @return 预估行数；预估失败时返回 0（调用方将走全量模式）
     */
    public int estimatedRowCount(File file) {
        AtomicInteger count = new AtomicInteger(0);

        try {
            FastExcel.read(file, new ReadListener<Map<Integer, String>>() {
                @Override
                public void invoke(Map<Integer, String> data, AnalysisContext context) {
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
