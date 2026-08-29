package com.wsw.fitnesssystem.shared.config;

import com.wsw.fitnesssystem.shared.config.properties.ThreadPoolProperties;
import lombok.RequiredArgsConstructor;
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
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置类
 *
 * <p>作为 {@link AsyncConfigurer} 的实现，统一管理 Spring {@code @Async} 注解使用的线程池。
 * 所有线程池参数均从 {@link ThreadPoolProperties} 配置类中读取，支持通过 YAML 文件动态调整，
 * 无需修改代码。</p>
 *
 * <p><b>三个核心线程池及其用途：</b></p>
 * <ul>
 *     <li><b>businessExecutor</b>（业务线程池，Bean 名称：{@value #BUSINESS_EXECUTOR}）<br>
 *         用于 IO 密集型任务，如 Excel 大文件导入、批量数据同步、文件上传/下载等。
 *         采用 {@link ThreadPoolExecutor.CallerRunsPolicy CallerRunsPolicy} 拒绝策略，
 *         当队列满时由调用线程执行，实现自然限流，保证任务不丢失。</li>
 *     <li><b>computeExecutor</b>（计算线程池，Bean 名称：{@value #COMPUTE_EXECUTOR}）<br>
 *         用于 CPU 密集型任务，如 BCrypt 密码加密、复杂规则计算、体测成绩评分等。
 *         线程数根据 CPU 核心数动态计算（公式：{@code 核心数 * cpuFactor + extraCores}），
 *         核心线程数 = 最大线程数，避免上下文切换开销。</li>
 *     <li><b>systemExecutor</b>（系统线程池，同时也是默认异步执行器，Bean 名称：{@value #SYSTEM_EXECUTOR}）<br>
 *         用于轻量级微任务，如审计日志、消息通知、埋点记录等。采用 {@link ThreadPoolExecutor.DiscardOldestPolicy DiscardOldestPolicy}
 *         拒绝策略，优先保证最新任务的执行。当 {@code @Async} 未指定线程池名称时，默认使用此池。</li>
 * </ul>
 *
 * <p><b>优雅关闭策略</b>：所有线程池在应用关闭时都会等待正在执行的任务完成，
 * 并设置最大等待时间（可配置），避免强制中断导致数据不一致。</p>
 *
 * <p><b>全局异常处理</b>：通过 {@link GlobalAsyncExceptionHandler} 捕获所有无返回值的
 * 异步方法的未处理异常，统一记录日志，防止异常被静默吞没。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/25 06:22
 * @since 1.0
 */
@Slf4j
@EnableAsync
@Configuration
@RequiredArgsConstructor
public class AsyncConfig implements AsyncConfigurer {

    private final ThreadPoolProperties poolProperties;

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
     *     <li>core=8：基础并发，匹配常见 8 核开发机</li>
     *     <li>max=12：突发时扩容，但不超过 12 避免 DB 连接池耗尽</li>
     *     <li>queue=100：缓冲突发导入请求，超过 100 说明系统过载</li>
     *     <li>CallerRunsPolicy：队列满时主线程执行，自然限流，保证任务不丢</li>
     * </ul>
     *
     * @return Executor
     */
    @Bean(BUSINESS_EXECUTOR)
    public Executor businessExecutor() {
        return createExecutor(
            poolProperties.getBusiness().getThreadPoolName(),
            poolProperties.getBusiness().getCorePoolSize(),
            poolProperties.getBusiness().getMaxPoolSize(),
            poolProperties.getBusiness().getQueueCapacity(),
            poolProperties.getBusiness().getThreadNamePrefix(),
            new ThreadPoolExecutor.CallerRunsPolicy(),
            poolProperties.getBusiness().getAwaitTerminationSeconds()
        );
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
        double cpuFactor = poolProperties.getCompute().getCpuFactor();
        int extraCores = poolProperties.getCompute().getExtraCores();
        int currentCores = Runtime.getRuntime().availableProcessors() ;
        int cpuCores = (int) (currentCores * cpuFactor + extraCores);

        return createExecutor(
            poolProperties.getCompute().getThreadPoolName(),
            cpuCores,
            cpuCores,  // CPU 密集型，max = core
            poolProperties.getCompute().getQueueCapacity(),
            poolProperties.getCompute().getThreadNamePrefix(),
            new ThreadPoolExecutor.CallerRunsPolicy(),
            poolProperties.getCompute().getAwaitTerminationSeconds()
        );
    }

    // ==================== 私有方法 ====================

    /**
     * 创建系统线程池（默认池）
     */
    private Executor createSystemExecutor() {
        return createExecutor(
            poolProperties.getSystem().getThreadPoolName(),
            poolProperties.getSystem().getCorePoolSize(),
            poolProperties.getSystem().getMaxPoolSize(),
            poolProperties.getSystem().getQueueCapacity(),
            poolProperties.getSystem().getThreadPoolName(),
            // DiscardOldestPolicy：丢弃最老任务，保证新任务低延迟
            // 审计日志旧记录可丢，新登录必须记录
            new ThreadPoolExecutor.DiscardOldestPolicy(),
            poolProperties.getSystem().getAwaitTerminationSeconds()
        );
    }

    /**
     * 通用线程池创建方法
     */
    private Executor createExecutor(
        String poolName, int corePoolSize, int maxPoolSize, int queueCapacity,
        String threadNamePrefix, RejectedExecutionHandler rejectedHandler, int awaitTerminationSeconds
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setRejectedExecutionHandler(rejectedHandler);
        executor.setWaitForTasksToCompleteOnShutdown(true); // 优雅关闭：等任务完成
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds); // 最多等n秒
        executor.initialize();

        log.info("[{}] Initialized: core={}, max={}, queue={}, await={}s",
            poolName, corePoolSize, maxPoolSize, queueCapacity, awaitTerminationSeconds);

        return executor;
    }

    // ==================== 内部类：全局异常处理器 ====================

    /**
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
