package com.wsw.fitnesssystem.handle_excel.core.executor;

import com.wsw.fitnesssystem.handle_excel.core.collector.ErrorCollector;
import com.wsw.fitnesssystem.handle_excel.core.collector.ErrorCollectorHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * CPU 密集型并行转换执行器
 * <p>供各 ImportAdapter 在 convert 阶段调用，实现"并行计算、串行持久化"</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/23 16:27
 * @since 1.0
 */
@Slf4j
@Component
public class ParallelConvertExecutor {

    private final Executor computeExecutor;

    public ParallelConvertExecutor(@Qualifier("computeExecutor") Executor computeExecutor) {
        this.computeExecutor = computeExecutor;
    }

    /**
     * 并行转换列表
     * <p>使用无锁并发设计：子线程只负责计算，结果在主线程串行汇总</p>
     *
     * @param list      待转换列表
     * @param converter 单条转换函数（必须线程安全、无状态）
     * @param rowExtractor 行号提取器（可从源对象中提取行号）
     * @param <T>       源类型
     * @param <R>       目标类型
     * @return 转换后的列表（失败项被静默跳过，但错误已记录）
     */
    public <T, R> List<R> execute(
        List<T> list, Function<T, R> converter, Function<T, Integer> rowExtractor) {

        if (list == null || list.isEmpty()) return List.of();

        List<R> results = new ArrayList<>();
        ErrorCollector collector = ErrorCollectorHolder.get();

        try {
            // 【阶段1：提交任务，并行执行】把每个元素扔到线程池，拿到一组 Future
            List<CompletableFuture<ConversionResult<R>>> futures = list
                .stream()
                .map(item -> submitConversion(item, converter))
                .toList();

            // 【阶段2：等待结果, 串行汇总（线程安全）】逐个调用 join，等所有任务完成，收集结果
            int i = 0;
            for (CompletableFuture<ConversionResult<R>> future : futures) {
                ConversionResult<R> result = future.join();
                if (result.isSuccess()) {
                    results.add(result.result());
                } else {
                    Throwable error = result.error();
                    T item = list.get(i);
                    int row = rowExtractor != null ? rowExtractor.apply(item) : -1;
                    collector.addError(row, "Conversion failed: " + error.getMessage());
                    log.warn("Conversion failed for one item", error);
                }
                i++;
            }
        } catch (Exception e) {
            // 【降级】并行出问题了，退回单线程处理
            log.warn("Parallel conversion failed, fallback to single-thread", e);
            for (T item : list) {
                try {
                    results.add(converter.apply(item));
                } catch (Exception ex) {
                    int row = rowExtractor != null ? rowExtractor.apply(item) : -1;
                    collector.addError(row, "Conversion failed: " + ex.getMessage());
                }
            }
        }

        return results;

    }

    /**
     * 提交单个转换任务到线程池
     * @param item      待转换的源元素
     * @param converter 单条转换函数，<b>必须线程安全、无状态</b>，因为会被多个工作线程并发调用
     * @return          异步任务凭证，任务完成后通过 {@link CompletableFuture#join()} 获取转换结果
     * @param <T>       源元素类型
     * @param <R>       转换后的目标类型
     */
    private <T, R> CompletableFuture<ConversionResult<R>> submitConversion(T item, Function<T, R> converter) {
        return CompletableFuture.supplyAsync(
            () -> {
                try {
                    return ConversionResult.success(converter.apply(item));
                } catch (Exception e) {
                    return ConversionResult.failure(e);
                }
            },
            this.computeExecutor
        );
    }

    private record ConversionResult<R>(R result, Throwable error) {
        public boolean isSuccess() {
            return error == null;
        }

        public static <R> ConversionResult<R> success(R result) {
            return new ConversionResult<>(result, null);
        }

        public static <R> ConversionResult<R> failure(Throwable error) {
            return new ConversionResult<>(null, error);
        }
    }

}
