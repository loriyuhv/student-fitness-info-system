package com.wsw.fitnesssystem.handle_excel.core.collector;

/**
 * 线程本地错误收集器（每个线程独立）
 * 用于在 processBatch → persist 之间传递错误收集器
 *
 * @author loriyuhv
 * @version 1.0 2026/8/29 15:26
 * @since 1.0
 */
public class ErrorCollectorHolder {

    private static final ThreadLocal<ErrorCollector> COLLECTOR = new ThreadLocal<>();

    public static void set(ErrorCollector collector) {
        COLLECTOR.set(collector);
    }

    public static ErrorCollector get() {
        ErrorCollector collector = COLLECTOR.get();
        if (collector == null) {
            collector = new ErrorCollector();
            COLLECTOR.set(collector);
        }
        return collector;
    }

    public static void remove() {
        COLLECTOR.remove();
    }

}
