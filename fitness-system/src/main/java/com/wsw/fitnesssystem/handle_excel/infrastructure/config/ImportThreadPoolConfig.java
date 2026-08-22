package com.wsw.fitnesssystem.handle_excel.infrastructure.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/20 13:18
 * @since 1.0
 */
@Slf4j
@Configuration
public class ImportThreadPoolConfig {
    /** corePoolSize IO密集型，核心线程数可略高于CPU核数 */
    private static final int CORE_POOL_SIZE = 8;
    /** maximumPoolSize 最大并发导入任务数 */
    private static final int MAX_POOL_SIZE = 16;
    /** 队列容量：缓冲突发流量 */
    private static final int QUEUE_CAPACITY = 200;
    /** keepAliveTime 空闲线程存活时间 */
    private static final long KEEP_ALIVE_SECONDS = 60L;

    @Bean("excelImportThreadPool")
    public ThreadPoolExecutor excelImportThreadPool() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadFactoryBuilder().setNameFormat("import-pool-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy() // 队列满时由调用线程执行，避免OOM
        );

        // 预启动核心线程，减少首次任务延迟
        executor.prestartAllCoreThreads();

        log.info("导入线程池初始化完成: core={}, max={}, queue={}",
                CORE_POOL_SIZE, MAX_POOL_SIZE, QUEUE_CAPACITY);

        return executor;
    }

}
