package com.wsw.fitnesssystem.handle_excel.core.executor;

import com.wsw.fitnesssystem.handle_excel.core.adapter.ImportAdapter;
import com.wsw.fitnesssystem.handle_excel.core.template.ExcelImportTemplate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 导入任务执行器
 * <p>负责任务提交到线程池，以及异常兜底</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 14:16
 * @since 1.0
 */
@Slf4j
@Component
public class ImportTaskExecutor {

    /** 获取线程池状态（用于监控）*/
    @Getter
    private final ThreadPoolExecutor executor;
    private final ExcelImportTemplate importTemplate;

    public ImportTaskExecutor(
            @Qualifier("excelImportThreadPool")  ThreadPoolExecutor executor,
            ExcelImportTemplate importTemplate) {
        this.executor = executor;
        this.importTemplate = importTemplate;
    }

    /**
     * 提交导入任务到线程池
     * @param taskId 任务ID
     * @param file Excel 文件
     * @param adapter 业务适配器
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void submit(String taskId, File file, ImportAdapter adapter) {
        log.info("[{}] 提交导入任务到线程池, bizType={}, file={}",
                taskId, adapter.getBizType(), file.getAbsolutePath());

        executor.execute(() -> {
            try {
                importTemplate.execute(taskId, file, adapter);
            } catch (Exception e) {
                // 最后一道防线：模板方法内部已有 try-catch，这里防止 Runnable 抛异常导致线程池静默吞掉
                log.error("[{}] 导入任务执行器捕获未处理异常", taskId, e);
            }
        });
    }

}
