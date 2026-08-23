package com.wsw.fitnesssystem.handle_excel.core.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
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

    private final ForkJoinPool convertPool;

    public ParallelConvertExecutor(@Qualifier("cpuIntensiveConvertPool") ForkJoinPool pool) {
        this.convertPool = pool;
    }

    /**
     * 并行转换列表
     *
     * @param list      待转换列表
     * @param converter 单条转换函数（必须线程安全、无状态）
     * @param <T>       源类型
     * @param <R>       目标类型
     * @return 转换后的列表
     */
    public <T, R> List<R> execute(List<T> list, Function<T, R> converter) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }

        try {
            // 在自定义 ForkJoinPool 中执行 parallelStream，
            // parallelStream 会自动使用该 Pool 而非 common pool
            return convertPool.submit(() ->
                    list.parallelStream()
                            .map(converter)
                            .toList()
            ).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("并行转换被中断，降级为单线程处理");
            return list.stream().map(converter).toList();
        } catch (Exception e) {
            log.warn("并行转换异常，降级为单线程处理", e);
            return list.stream().map(converter).toList();
        }
    }

}
