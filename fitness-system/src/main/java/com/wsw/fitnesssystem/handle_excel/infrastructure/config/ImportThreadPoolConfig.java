package com.wsw.fitnesssystem.handle_excel.infrastructure.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
@Configuration
public class ImportThreadPoolConfig {

    @Bean("excelImportThreadPool")
    public ThreadPoolExecutor excelImportThreadPool() {
        return new ThreadPoolExecutor(
                8, // corePoolSize IO密集型，核心线程数可略高于CPU核数
                16, // maximumPoolSize 最大并发导入任务数
                60L, TimeUnit.SECONDS, // keepAliveTime 空闲线程存活时间
                new LinkedBlockingQueue<>(200), // 队列容量：缓冲突发流量
                new ThreadFactoryBuilder().setNameFormat("import-pool-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy() // 队列满时由调用线程执行，避免OOM
        );
    }
}
