package com.wsw.fitnesssystem.handle_excel.application.scheduler;

import com.wsw.fitnesssystem.handle_excel.application.orchestration.ImportOrchestrator;
import com.wsw.fitnesssystem.handle_excel.application.plugin.ImportPlugin;
import com.wsw.fitnesssystem.handle_excel.application.port.output.DistributedLockPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 导入任务执行器
 * <p>负责任务提交到线程池，以及异常兜底、文件锁释放</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 14:16
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncImportScheduler {

    private final ImportOrchestrator importTemplate;
    private final DistributedLockPort distributedLockPort;

    /**
     * 提交导入任务到线程池
     *
     * @param taskId 任务ID
     * @param file Excel 文件
     * @param adapter 业务适配器
     * @param md5     文件 MD5（用于任务完成后释放防重锁）
     */
    @Async("businessExecutor")
    public void submit(String taskId, File file, ImportPlugin<?, ?> adapter, String md5) {
        long taskStart = System.currentTimeMillis();  // ← 任务开始时间

        log.info("[{}] Import task submitted to thread pool, bizType={}, file={}, md5={}",
            taskId, adapter.getBizType(), file.getAbsolutePath(), md5);

        try {
            importTemplate.execute(taskId, file, adapter);
        } catch (Exception e) {
            // 最后一道防线：模板方法内部已有 try-catch，这里防止 Runnable 抛异常导致线程池静默吞掉
            log.error("[{}] Import task executor caught unhandled exception", taskId, e);
        } finally {
            long totalDuration = System.currentTimeMillis() - taskStart;  // ← 总耗时
            log.info("[{}] 导入任务结束，总耗时: {}ms ({}s)", taskId, totalDuration, totalDuration / 1000);
            // 任务结束（成功/失败/异常）后，主动释放文件锁
            // Redis TTL 作为兜底，防止进程崩溃导致锁永久泄漏
            if (StringUtils.isNotBlank(md5)) {
                distributedLockPort.releaseLock(md5);
            }
        }
    }

}
