package com.wsw.fitnesssystem.data_exchange.application.scheduler;

import com.wsw.fitnesssystem.data_exchange.application.orchestration.ImportOrchestrator;
import com.wsw.fitnesssystem.data_exchange.application.plugin.ImportPlugin;
import com.wsw.fitnesssystem.data_exchange.application.port.output.DistributedLockPort;
import com.wsw.fitnesssystem.data_exchange.application.service.command.ImportSubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 异步导入任务调度器。
 * <p>
 * <b>职责：</b>
 * <ul>
 *   <li>作为 {@code @Async} 方法的入口，将导入任务提交到业务线程池</li>
 *   <li>统一管理任务生命周期：执行前记录，执行后释放分布式锁</li>
 *   <li>作为异常兜底防线，防止未捕获异常导致线程池静默吞掉</li>
 * </ul>
 * <p>
 * <b>为什么需要单独一个类？</b>
 * <ul>
 *   <li>Spring {@code @Async} 需要通过代理调用，自调用无效</li>
 *   <li>锁的获取在同步入口（{@link ImportSubmissionService}），释放由调度器统一管理</li>
 *   <li>将"异步机制"与"业务编排"（{@link ImportOrchestrator}）解耦</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 14:16
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncImportScheduler {

    private final ImportOrchestrator importOrchestrator;
    private final DistributedLockPort distributedLockPort;

    /**
     * 异步执行导入任务。
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>记录任务开始时间和日志</li>
     *   <li>调用编排器执行业务逻辑（解析 → 校验 → 转换 → 持久化）</li>
     *   <li>finally 块中释放文件锁（无论成功或失败）</li>
     * </ol>
     * <p>
     * <b>锁的生命周期：</b>
     * <ul>
     *   <li>获取锁：{@link ImportSubmissionService#submit} 同步阶段完成</li>
     *   <li>释放锁：本方法的 finally 块完成</li>
     *   <li>Redis TTL 作为兜底，防止进程崩溃导致锁永久泄漏</li>
     * </ul>
     *
     * @param taskId 任务ID
     * @param file Excel 文件
     * @param plugin 业务适配器
     * @param md5     文件 MD5（用于任务完成后释放防重锁）
     */
    @Async("businessExecutor")
    public void submit(String taskId, File file, ImportPlugin<?, ?> plugin, String md5) {

        long startTime = System.currentTimeMillis();  // ← 任务开始时间

        log.info("[{}] Async task started, bizType={}, file={}",
            taskId, plugin.getBizType(), file.getAbsolutePath());

        try {
            importOrchestrator.execute(taskId, file, plugin);
        } catch (Exception e) {
            // 最后一道防线：orchestrator 内部已有异常处理，这里兜底防止线程池静默吞掉
            log.error("[{}] Unhandled exception in async task", taskId, e);
        } finally {
            long elapsed = System.currentTimeMillis() - startTime; // ← 总耗时
            log.info("[{}] Async task finished, elapsed={}ms ({}s)", taskId, elapsed, elapsed / 1000);
            // 释放文件锁（如果 MD5 有效）。携带 taskId 做持有者校验，防止误删他任务锁
            if (StringUtils.isNotBlank(md5)) {
                distributedLockPort.releaseLock(md5, taskId);
                log.debug("[{}] File lock released, md5={}", taskId, md5);
            }
        }
    }

}
