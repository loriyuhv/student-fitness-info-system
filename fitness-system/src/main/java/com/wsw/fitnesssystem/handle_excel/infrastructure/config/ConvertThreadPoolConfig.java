package com.wsw.fitnesssystem.handle_excel.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CPU 密集型转换线程池配置
 * <p>职责：为密码加密、复杂公式计算等纯内存/CPU 操作提供并行能力</p>
 * <p>设计要点：</p>
 * <li>1. 线程数 = CPU 核数（计算密集型，避免上下文切换）</li>
 * <li>2. 使用 ForkJoinPool，配合 parallelStream 实现任务窃取</li>
 * <li>3. 实现 DisposableBean，保证 Spring 关闭时优雅 shutdown</li>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/23 16:24
 * @since 1.0
 */
@Slf4j
@Configuration
public class ConvertThreadPoolConfig implements DisposableBean {

    private volatile ForkJoinPool convertPool;

    @Bean("cpuIntensiveConvertPool")
    public ForkJoinPool cpuIntensiveConvertPool() {
        // CPU 密集型：线程数 = 物理核心数，超线程不会带来收益
        int parallelism = Runtime.getRuntime().availableProcessors() - 2;

        this.convertPool = new ForkJoinPool(
                parallelism,
                new ForkJoinPool.ForkJoinWorkerThreadFactory() {
                    private final AtomicInteger count = new AtomicInteger(0);

                    @Override
                    public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
                        ForkJoinWorkerThread thread =
                                ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                        thread.setName("convert-pool-" + count.incrementAndGet());
                        return thread;
                    }
                },
                (t, e) -> log.error("CPU密集型转换线程异常, thread={}", t.getName(), e),
                false
        );

        log.info("CPU密集型转换线程池初始化完成, parallelism={}", parallelism);
        return convertPool;
    }

    @Override
    public void destroy() throws Exception {
        if (convertPool != null) {
            log.info("正在关闭CPU密集型转换线程池...");
            convertPool.shutdown();
            if (!convertPool.awaitTermination(60, TimeUnit.SECONDS)) {
                log.warn("CPU密集型转换线程池未在60秒内关闭，强制中断");
                convertPool.shutdownNow();
            }
        }
    }

}
