package com.wsw.fitnesssystem.handle_excel.core.executor;

import com.wsw.fitnesssystem.handle_excel.core.adapter.ImportAdapter;
import com.wsw.fitnesssystem.handle_excel.core.port.ImportFileLockPort;
import com.wsw.fitnesssystem.handle_excel.core.template.ExcelImportTemplate;
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
public class ImportTaskExecutor {

    private final ExcelImportTemplate importTemplate;
    private final ImportFileLockPort importFileLockPort;

    /**
     * 提交导入任务到线程池
     *
     * @param taskId 任务ID
     * @param file Excel 文件
     * @param adapter 业务适配器
     * @param md5     文件 MD5（用于任务完成后释放防重锁）
     */
    @Async("businessExecutor")
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void submit(String taskId, File file, ImportAdapter adapter, String md5) {
        log.info("[{}] 提交导入任务到线程池, bizType={}, file={}, md5={}",
                taskId, adapter.getBizType(), file.getAbsolutePath(), md5);
        try {
            importTemplate.execute(taskId, file, adapter);
        } catch (Exception e) {
            // 最后一道防线：模板方法内部已有 try-catch，这里防止 Runnable 抛异常导致线程池静默吞掉
            log.error("[{}] 导入任务执行器捕获未处理异常", taskId, e);
        } finally {
            // 任务结束（成功/失败/异常）后，主动释放文件锁
            // Redis TTL 作为兜底，防止进程崩溃导致锁永久泄漏
            if (StringUtils.isNotBlank(md5)) {
                importFileLockPort.releaseLock(md5);
            }
        }
    }

}
