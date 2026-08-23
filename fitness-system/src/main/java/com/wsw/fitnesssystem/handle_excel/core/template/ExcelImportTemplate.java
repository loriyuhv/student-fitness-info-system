package com.wsw.fitnesssystem.handle_excel.core.template;

import com.wsw.fitnesssystem.handle_excel.core.adapter.ImportAdapter;
import com.wsw.fitnesssystem.handle_excel.core.exception.ExcelException;
import com.wsw.fitnesssystem.handle_excel.core.parser.ExcelParser;
import com.wsw.fitnesssystem.handle_excel.core.port.ImportProgressPort;
import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ExcelConstants;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 导入模板方法
 * <p>定义标准导入流程，与具体业务完全解耦</p>
 *
 * <p>标准流程：</p>
 * <li>1. 解析 Excel（通用）</li>
 * <li>2. 业务校验（适配器实现）</li>
 * <li>3. 数据转换（适配器实现）</li>
 * <li>4. 批量持久化（适配器实现）</li>
 * <li>5. 上报进度（通用）</li>
 * <li>6. 完成/异常处理（通用）</li>
 *
 * <p>异常处理策略：</p>
 * <li>1. 解析异常：任务终止，status=FAILED</li>
 * <li>2. 单批异常：故障隔离，继续下一批，status=PARTIAL</li>
 * <li>3. 全局异常：任务终止，status=FAILED</li>
 * <li>4. 临时文件：finally 强制清理</li>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 12:22
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExcelImportTemplate {

    private final ExcelParser excelParser;
    private final ImportProgressPort importProgressPort;

    /**
     * 执行导入（模板方法）
     *
     * @param taskId 任务 ID
     * @param file Excel 文件（磁盘临时文件）
     * @param adapter 业务适配器
     * @param <T> DTO 类型
     * @param <E> Entity 类型
     */
    public <T, E> void execute(String taskId, File file, ImportAdapter<T, E> adapter) {
        int total;
        int successCount = 0;
        int failCount = 0;
        List<String> errorMsgList = new ArrayList<>();

        try {
            // ========== Step 1: 解析 Excel ==========
            log.info("[{}] 开始解析 Excel, adapter={}, dtoClass={}",
                    taskId, adapter.getBizType(), adapter.getDtoClass().getSimpleName());

            List<T> list = excelParser.parse(file, adapter.getDtoClass());
            total = list.size();

            if (total == 0) {
                log.warn("[{}] Excel 文件为空或无可解析数据", taskId);
                importProgressPort.fail(taskId, "Excel 文件为空或无可解析数据");
                return;
            }

            log.info("[{}] Excel 解析完成，共 {} 条数据", taskId, total);

            // ========== Step 2: 初始化进度 ==========
            importProgressPort.init(taskId, total);

            // ========== Step 3: 分片处理 ==========
            int batchSize = adapter.getBatchSize();
            List<List<T>> batches = partition(list, batchSize);
            log.info("[{}] 分片完成，共 {} 批，每批 {} 条", taskId, batches.size(), batchSize);

            for (int i = 0; i < batches.size(); i++) {
                List<T> batch = batches.get(i);
                try {
                    // 3.1 业务校验（适配器实现）
                    List<T> validated = adapter.validate(batch);
                    int filtered = batch.size() - validated.size();
                    if (filtered > 0) {
                        log.info("[{}] 第 {} 批过滤 {} 条重复/非法数据", taskId, i + 1, filtered);
                    }

                    if (validated.isEmpty()) {
                        failCount += batch.size();
                        addErrorMsg(errorMsgList, "第" + (i + 1) + "批数据全部校验失败");
                        importProgressPort.updateProgress(taskId, successCount, failCount, errorMsgList);
                        continue;
                    }

                    // 3.2 数据转换（适配器实现）
                    List<E> entities = adapter.convert(validated);

                    // 3.3 批量持久化（适配器实现）
                    adapter.persist(entities);

                    successCount += validated.size();
                    failCount += (batch.size() - validated.size());

                    log.info("[{}] 第 {}/{} 批处理完成，success={}, fail={}",
                            taskId, i + 1, batches.size(), validated.size(), filtered);

                } catch (Exception e) {
                    // 故障隔离：单批失败不终止整个任务
                    log.error("[{}] 第 {} 批处理失败, batchSize={}", taskId, i + 1, batch.size(), e);
                    failCount += batch.size();
                    addErrorMsg(errorMsgList, "第" + (i + 1) + "批:" + truncate(e.getMessage()));
                }

                // 3.4 更新进度
                importProgressPort.updateProgress(taskId, successCount, failCount, errorMsgList);
            }

            // ========== Step 4: 完成 ==========
            if (failCount == 0) {
                importProgressPort.finish(taskId, successCount);
                log.info("[{}] 导入任务全部成功完成, total={}, success={}", taskId, total, successCount);
            } else {
                importProgressPort.partial(taskId, successCount, failCount, errorMsgList);
                log.info("[{}] 导入任务部分完成, total={}, success={}, fail={}",
                    taskId, total, successCount, failCount);
            }
        } catch (ExcelException e) {
            // Excel 模块已知异常（解析失败等）
            String defaultMsg = e.getResultCode().getMessage();
            String customMsg = e.getMessage();
            String finalMsg = defaultMsg + "：" + customMsg;
            log.error("[{}] 导入任务业务异常: {}", taskId, finalMsg, e);
            importProgressPort.fail(taskId, finalMsg);
        } catch (Exception e) {
            // 未知异常兜底
            log.error("[{}] 导入任务异常终止", taskId, e);
            importProgressPort.fail(taskId, ResultCode.SERVER_TEMP_ERROR.getMessage());
        } finally {
            // ========== Step 5: 清理临时文件 ==========
            cleanup(file);
        }
    }

    /**
     * 安全添加错误信息，防止内存无限增长
     * 仅保留最近 {@link ExcelConstants#ERROR_MSG_MAX_COUNT} 条
     * @param errorMsgList 错误信息列表
     * @param msg 错误信息
     */
    private void addErrorMsg(List<String> errorMsgList, String msg) {
        if (errorMsgList.size() < ExcelConstants.ERROR_MSG_MAX_COUNT) {
            errorMsgList.add(msg);
        }
    }

    /**
     * 分片工具：将列表按指定大小切分
     * 使用 new ArrayList 复制子列表，避免 subList 视图特性导致并发问题
     * @param list 列表
     * @param size 大小
     * @return 切片后的列表数据
     * @param <T> Dto
     */
    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            result.add(new ArrayList<>(list.subList(i, Math.min(i + size, list.size()))));
        }
        return result;
    }

    /**
     * 清理临时文件及父目录
     * @param file Excel文件
     */
    private void cleanup(File file) {
        if (file == null) return;
        try {
            if (file.exists()) {
                FileUtils.delete(file);
                log.info("临时文件已删除: {}", file.getAbsolutePath());
            }
            File parent = file.getParentFile();
            if (parent != null && parent.exists()) {
                FileUtils.deleteDirectory(parent);
                log.info("临时目录已删除: {}", parent.getAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("临时文件清理失败, path={}", file.getAbsolutePath(), e);
        }
    }

    /**
     * 截断字符串，防止存入 Redis 过长
     *
     * @param str 字符串
     * @return 截断后的字符串
     */
    private String truncate(String str) {
        if (str == null) return "";
        return str.length() > 80 ? str.substring(0, 80) + "..." : str;
    }
}
