package com.wsw.fitnesssystem.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/25 06:22
 * @since 1.0
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    // ==================== 常量配置 ====================

    /** 业务线程池：Excel 导入、批量写入等 IO 密集型任务 */
    private static final String BUSINESS_EXECUTOR = "businessExecutor";
    /** 系统线程池：审计日志、通知、埋点等微任务 */
    private static final String SYSTEM_EXECUTOR = "systemExecutor";
    /** 计算线程池：密码加密 */
    public static final String COMPUTE_EXECUTOR = "computeExecutor";

    // ==================== AsyncConfigurer 接口实现 ====================

    /**
     * 默认异步执行器
     * <p>
     * 当 {@code @Async} 不指定名字时，使用此执行器。
     * 设置为 systemExecutor，因为系统微任务是最常见的异步场景。
     * </p>
     *
     * @return SystemExecutor
     */
    @Override
    @Bean(name = SYSTEM_EXECUTOR)
    public Executor getAsyncExecutor() {
        return createSystemExecutor();
    }

    /**
     * 全局异步异常处理器
     * <p>
     * 捕获所有 {@code @Async} 方法的未处理异常（包括 void 返回值的异步方法）。
     * 有返回值的异常（如 {@code CompletableFuture}）由调用方处理，不走这里。
     * </p>
     * @return AsyncUncaughtExceptionHandler
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new GlobalAsyncExceptionHandler();
    }

    // ==================== 命名线程池 Bean ====================

    /**
     * 业务线程池（IO 密集型）
     * <p>
     * 用途：
     * <ul>
     *     <li>Excel 大文件导入（EasyExcel 流式读取 + 批量写入）</li>
     *     <li>批量数据同步、数据迁移</li>
     *     <li>文件上传/下载处理</li>
     * </ul>
     *
     * <p>配置 rationale：</p>
     * <ul>
     *     <li>core=4：基础并发，匹配常见 4 核开发机</li>
     *     <li>max=10：突发时扩容，但不超过 10 避免 DB 连接池耗尽</li>
     *     <li>queue=100：缓冲突发导入请求，超过 100 说明系统过载</li>
     *     <li>CallerRunsPolicy：队列满时主线程执行，自然限流，保证任务不丢</li>
     * </ul>
     *
     * @return Executor
     */
    @Bean(BUSINESS_EXECUTOR)
    public Executor businessExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("business-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);      // 优雅关闭：等任务完成
        executor.setAwaitTerminationSeconds(60);                 // 最多等 60 秒
        executor.initialize();

        printExecutorInfo(
            BUSINESS_EXECUTOR, executor.getCorePoolSize(),
            executor.getMaxPoolSize(), executor.getQueueCapacity()
        );

        return executor;
    }

    /**
     * 计算型线程池（CPU 密集型）
     * <p>
     * 用途：
     * <ul>
     *     <li>BCrypt 密码加密（批量导入时）</li>
     *     <li>复杂数据清洗规则引擎</li>
     *     <li>体测成绩评分计算（BMI、肺活量标准分转换）</li>
     * </ul>
     *
     * <p>设计 rationale：</p>
     * <ul>
     *     <li>core = max = CPU 核数：CPU 密集型任务线程数超过核数反而因上下文切换降低效率</li>
     *     <li>queue = 50：计算任务不能无限堆积，队列满说明系统 CPU 饱和</li>
     *     <li>CallerRunsPolicy：队列满时主线程执行，自然限流，避免 OOM</li>
     * </ul>
     * @return 计算线程池对象
     */
    @Bean(COMPUTE_EXECUTOR)
    public Executor computeExecutor() {
        int cpuCores = Runtime.getRuntime().availableProcessors() / 4 + 2;

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(cpuCores);
        executor.setMaxPoolSize(cpuCores);  // CPU 密集型，线程数 = (核数 / 4) + 2，不扩容
        executor.setQueueCapacity(50);       // 小队列，快速失败或限流
        executor.setThreadNamePrefix("compute-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);  // 计算任务可能长，多给点时间
        executor.initialize();

        log.info("[{}] CPU线程池初始化: core={}, max={}, queue=50 (CPU核数)",
            COMPUTE_EXECUTOR, cpuCores, cpuCores);
        return executor;
    }

    // ==================== 私有方法 ====================

    /**
     * 创建系统线程池（默认池）
     */
    private Executor createSystemExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("system-");
        // DiscardOldestPolicy：丢弃最老任务，保证新任务低延迟
        // 审计日志旧记录可丢，新登录必须记录
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        printExecutorInfo(
            SYSTEM_EXECUTOR, executor.getCorePoolSize(),
            executor.getMaxPoolSize(), executor.getQueueCapacity()
        );

        return executor;
    }

    private void printExecutorInfo(
        String executorName, int corePoolSize, int maxPoolSize, int queueSize) {
        log.info("[{}] 初始化完成: core={}, max={}, queue={}",
            executorName, corePoolSize, maxPoolSize, queueSize);
    }

    // ==================== 内部类：全局异常处理器 ====================

    /**
     *
     * 全局异步未捕获异常处理器
     */
    public static class GlobalAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        @Override
        public void handleUncaughtException(
            @NonNull Throwable ex, Method method, Object @NonNull ... params) {
            log.error("异步方法执行异常: {}.{}, 参数: {}",
                method.getDeclaringClass().getSimpleName(),
                method.getName(),
                params, ex
            );
        }

    }

}
