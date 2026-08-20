package com.wsw.fitnesssystem.handle_excel.infrastructure.async;

import com.wsw.fitnesssystem.handle_excel.application.dto.UserExcelDTO;
import com.wsw.fitnesssystem.handle_excel.domain.model.User;
import com.wsw.fitnesssystem.handle_excel.domain.service.UserImportDomainService;
import com.wsw.fitnesssystem.handle_excel.infrastructure.excel.ExcelParser;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.UserBatchRepository;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.assembler.UserAssembler;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.entity.SysUser;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.redis.ImportProgressRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异常处理 + 查重 + 统计 + 清理
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:51
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImportTaskExecutor {
    private final ExcelParser excelParser;
    private final UserImportDomainService domainService;
    private final UserBatchRepository userBatchRepository;
    private final ImportProgressRepository progressRepository;
    private final UserAssembler userAssembler;

    @Resource(name ="excelImportThreadPool")
    private ThreadPoolExecutor executor;

    private static final int BATCH_SIZE = 500;

    /**
     * 提交任务到自定义线程池
     * @param taskId 任务ID
     * @param filePath 文件路径
     */
    public void submit(String taskId, String filePath) {
        executor.execute(() -> doImport(taskId, filePath));
    }

    /**
     * 执行导入（全链路异常捕获 + 临时文件清理）
     * @param taskId 任务ID
     * @param filePath 文件路径
     */
    public void doImport(String taskId, String filePath) {
        File file = new File(filePath);
        int total;
        int successCount = 0;
        int failCount = 0;
        List<String> errorMsgList = new ArrayList<>();

        try {
            // 1. 解析 Excel
            List<UserExcelDTO> list = excelParser.parse(file);
            total = list.size();

            if (total == 0) {
                progressRepository.fail(taskId, "Excel 文件为空");
                return;
            }

            // 2. 初始化 Redis 进度
            progressRepository.init(taskId, total);

            // 3. 分片处理
            for (List<UserExcelDTO> batch : partition(list, BATCH_SIZE)) {

                try {
                    // 3.1 校验 + 转换（含批量查重）
                    List<User> users = domainService.validateAndConvert(batch);

                    if (users.isEmpty()) {
                        failCount += batch.size();
                        errorMsgList.add("整批数据校验失败或全部重复");
                        progressRepository.updateProgress(
                                taskId, successCount, failCount, total, errorMsgList);
                        continue;
                    }

                    // 3.2 Domain → Entity
                    List<SysUser> entities = userAssembler.toEntityList(users);

                    // 3.3 批量插入（每批独立事务）
                    userBatchRepository.batchInsert(entities);
                    successCount += users.size();
                    failCount += (batch.size() - users.size());
                } catch (Exception e) {
                    log.error("批次处理失败, taskId={}, batchSize={}", taskId, batch.size(), e);
                    failCount += batch.size();
                    String msg = truncate(e.getMessage(), 100);
                    errorMsgList.add(msg);
                }

                // 3.4 更新 Redis 进度
                progressRepository.updateProgress(
                        taskId, successCount, failCount, total, errorMsgList);
            }

            // 4. 完成
            if (failCount == 0) {
                progressRepository.finish(taskId, successCount);
            } else {
                progressRepository.partial(taskId, successCount, failCount, errorMsgList);
            }

            log.info("导入任务完成, taskId={}, total={}, success={}, fail={}",
                    taskId, total, successCount, failCount);
        } catch (Exception e) {
            log.error("导入任务异常终止, taskId={}", taskId, e);
            progressRepository.fail(taskId, truncate(e.getMessage(), 200));
        } finally {
            // 5. 清理临时文件
            cleanup(file);
        }
    }

    /**
     * 每批独立事务：即使某一批失败，也只回滚当前批，不影响已成功的批次
     * @param entities 插入元素集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchInsertWithTx(List<SysUser> entities) {
        userBatchRepository.batchInsert(entities);
    }

    /**
     * 分片工具
     * @param list 信息集合
     * @param size 大小
     * @return 分片
     */
    private List<List<UserExcelDTO>> partition(List<UserExcelDTO> list, int size) {
        List<List<UserExcelDTO>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            result.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return result;
    }


    /**
     * 清理临时文件
     * @param file 文件
     */
    private void cleanup(File file) {
        try {
            if (file != null && file.exists()) {
                FileUtils.delete(file);
            }

            File parent = file != null ? file.getParentFile() : null;
            if (parent != null && parent.exists()) {
                FileUtils.deleteDirectory(parent);
            }
        } catch (Exception e) {
            log.warn("临时文件清理失败, path={}", file != null ? file.getAbsolutePath() : "null", e);
        }
    }

    /**
     * 截断字符串
     * @param str 字符串
     * @param maxLength 最大长度
     * @return 截断后的字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }
}
