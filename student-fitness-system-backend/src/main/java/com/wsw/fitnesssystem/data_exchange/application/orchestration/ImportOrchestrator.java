package com.wsw.fitnesssystem.data_exchange.application.orchestration;

import com.google.common.collect.Lists;
import com.wsw.fitnesssystem.data_exchange.application.config.ImportApplicationProperties;
import com.wsw.fitnesssystem.data_exchange.application.plugin.ImportPlugin;
import com.wsw.fitnesssystem.data_exchange.application.collector.ErrorCollector;
import com.wsw.fitnesssystem.data_exchange.application.collector.ErrorCollectorHolder;
import com.wsw.fitnesssystem.data_exchange.application.port.output.FileParsingPort;
import com.wsw.fitnesssystem.data_exchange.application.port.output.FileStoragePort;
import com.wsw.fitnesssystem.data_exchange.domain.model.ImportTask;
import com.wsw.fitnesssystem.data_exchange.domain.exception.ImportCancelledException;
import com.wsw.fitnesssystem.data_exchange.application.collector.ErrorRecord;
import com.wsw.fitnesssystem.data_exchange.domain.repository.ImportTaskRepository;
import com.wsw.fitnesssystem.data_exchange.application.generator.ErrorFileGenerator;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 导入流程编排器。
 * <p>
 * 定义标准导入流程，与具体业务完全解耦。所有业务导入只需实现 {@link ImportPlugin} 即可接入。
 * </p>
 *
 * <p><b>核心设计：</b></p>
 * <ol>
 *     <li>双模式解析：小文件走全量（简单高效），大文件走流式（内存安全）</li>
 *     <li>批处理抽象：全量/流式两种模式复用同一套 {@link #processBatch} 逻辑</li>
 *     <li>故障隔离：单批失败不影响其他批次，最终状态为 PARTIAL</li>
 *     <li>资源兜底：finally 强制清理临时文件，防止磁盘泄漏</li>
 * </ol>
 *
 * <p><b>内存安全策略：</b></p>
 * <ul>
 *     <li>小文件（&lt; 1万行）：全量解析，代码简单，内存可控</li>
 *     <li>大文件（&ge; 1万行）：流式解析，每批处理完立即释放，内存占用 O(batchSize)</li>
 * </ul>
 *
 * <p><b>异常处理策略：</b></p>
 * <ul>
 *     <li>解析异常：任务终止，状态 = FAILED</li>
 *     <li>单批异常：故障隔离，继续下一批，状态 = PARTIAL</li>
 *     <li>全局异常：任务终止，状态 = FAILED</li>
 *     <li>临时文件：finally 强制清理</li>
 * </ul>
 *
 * <p><b>标准流程：</b></p>
 * <ol>
 *     <li>预估行数，智能选择全量/流式解析（通用）</li>
 *     <li>业务校验（插件实现）</li>
 *     <li>数据转换（插件实现）</li>
 *     <li>批量持久化（插件实现）</li>
 *     <li>上报进度（通用）</li>
 *     <li>完成/异常处理（通用）</li>
 * </ol>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 12:22
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImportOrchestrator {

    private final FileStoragePort fileStoragePort;
    private final FileParsingPort fileParsingPort;
    private final ErrorFileGenerator errorFileGenerator;
    private final ImportTaskRepository importTaskRepository;
    private final ImportApplicationProperties appProperties;

    /**
     * 执行导入（模板方法）
     * <p><b>完整流程：</b></p>
     * <ol>
     *   <li>预估文件数据行数，智能选择全量/流式模式</li>
     *   <li>进入对应执行分支（{@link #doExecuteFull} 或 {@link #doExecuteStream}）</li>
     *   <li>分支内部：解析 → 校验 → 转换 → 持久化 → 上报进度</li>
     *   <li>最终状态判定：全部成功 = FINISHED，部分失败 = PARTIAL</li>
     *   <li>finally 强制清理临时文件</li>
     * </ol>
     *
     * @param taskId 任务唯一标识，用于进度追踪与日志串联
     * @param file 已转存到磁盘的临时文件（非 MultipartFile，避免 InputStream 异步关闭）
     * @param plugin 业务插件，封装了具体业务的校验/转换/持久化逻辑
     * @param <T> 文件解析对应的 DTO 类型
     * @param <E> 持久化对应的 Entity 类型
     */
    public <T, E> void execute(String taskId, File file, ImportPlugin<T, E> plugin) {

        try {
            // ========== Step 1: 预估行数，决策解析模式 ==========
            int estimatedRows = fileParsingPort.estimatedRowCount(file);
            int batchSize = plugin.getBatchSize();
            if (estimatedRows < appProperties.getBatch().getStreamThreshold()) {
                // 小文件：全量解析，代码简单，内存 = O(total)
                log.info("[{}] Estimated {} rows, using full processing mode", taskId, estimatedRows);
                doExecuteFull(taskId, file, plugin);
            } else {
                // 大文件：真流式解析，内存 = O(batchSize)，与文件大小无关
                log.info("[{}] Estimated {} rows, using streaming processing mode, batchSize={}",
                    taskId, estimatedRows, batchSize);
                doExecuteStream(taskId, file, plugin, batchSize, estimatedRows);
            }
        } catch (ImportCancelledException e) {
            log.warn("[{}] Task cancelled by user", taskId);
        } catch (BizException e) {
            // 文件解析已知异常（格式损坏、密码保护、解析失败等）
            String customMsg = e.getMessage();
            log.error("[{}] File processing failed: {}", taskId, customMsg, e);
            // 失败时标记任务为 FAILED（任务已存在则迁移状态，未创建则新建后标记）
            markTaskFailed(taskId, customMsg);
        } catch (Exception e) {
            // 未知异常兜底：防止任何未捕获异常导致任务状态悬空
            log.error("[{}] Import task terminated abnormally", taskId, e);
            markTaskFailed(taskId, ResultCode.SERVER_TEMP_ERROR.getMessage());
        } finally {
            // ========== Step 5: 清理临时文件（强制兜底） ==========
            fileStoragePort.cleanup(file);
            // ========== Step 6：清理 ThreadLocal ==========
            ErrorCollectorHolder.remove();
        }

    }

    /**
     * 全量模式执行（小文件 &lt; 1万行）
     *
     * <p><b>流程：</b></p>
     * <ol>
     *   <li>一次性解析完整文件到 List</li>
     *   <li>空文件校验：直接标记 FAILED 并返回</li>
     *   <li>创建聚合根并启动任务（状态存入 Redis）</li>
     *   <li>按 batchSize 分片为若干批次</li>
     *   <li>逐批调用 {@link #processBatch} 处理</li>
     *   <li>每批结束后更新 Redis 进度</li>
     *   <li>全部批次完成后标记最终状态</li>
     * </ol>
     *
     * @param taskId 任务唯一标识
     * @param file 临时 Excel 文件
     * @param plugin 业务插件
     * @param <T> Excel 解析对应的 DTO 类型
     * @param <E> 持久化对应的 Entity 类型
     */
    private <T, E> void doExecuteFull(
            String taskId, File file, ImportPlugin<T, E> plugin) {

        // 1. 全量解析：一次性读入内存，适合小文件
        List<T> list = fileParsingPort.parseFull(file, plugin.getDtoClass(), taskId);
        int total = list.size();

        // 2. 空文件防御：无可解析数据时直接失败，避免无意义轮询
        if (total == 0) {
            log.warn("[{}] Excel file is empty or no data to parse", taskId);
            ImportTask task = new ImportTask(taskId);
            task.fail("文件内容为空，无可解析的数据");
            importTaskRepository.save(task);
            return;
        }

        // 3. 创建聚合根并启动任务（状态存入 Redis）
        ImportTask task = new ImportTask(taskId);
        task.start(total);
        importTaskRepository.save(task);

        // 4. 初始化全局错误收集器（懒加载，自动创建）
        ErrorCollectorHolder.remove(); // 清理旧数据（防御性）
        ErrorCollector collector = ErrorCollectorHolder.get(); // 懒加载创建

        // 5. 分片：将全量 List 切分为固定大小的批次
        int batchSize = plugin.getBatchSize();
        List<List<T>> batches = Lists.partition(list, batchSize);
        log.info("[{}] Sharding completed, {} batches, batch size: {} rows",
            taskId, batches.size(), batchSize);

        // 6. 逐批处理：累加成功/失败计数
        for (int i = 0; i < batches.size(); i++) {
            // 6.1 每批处理前检查取消
            checkCancelled(task, taskId);

            // 6.2 处理单批：校验 → 转换 → 持久化
            List<T> batch = batches.get(i);
            BatchResult result = processBatch(taskId, batch, plugin, i + 1);

            // 6.3 实时上报进度：客户端轮询可感知到处理进展
            List<String> latestSummary = buildErrorSummary(collector);
            task.recordBatch(result.successIncrement, result.failIncrement, latestSummary);
            importTaskRepository.save(task);
        }

        // 7. 最终状态判定 + 错误文件
        if (collector.hasErrors()) {
            saveErrorFile(taskId, collector, plugin);
        }

        if (task.getFailCount() == 0) {
            task.finishSuccess();
        } else {
            task.finishPartial();
        }
        importTaskRepository.save(task);

        log.info("[{}] Import completed, success={}, fail={}",
            taskId, task.getSuccessCount(), task.getFailCount());

        // 8. 清理 ThreadLocal
        ErrorCollectorHolder.remove();

    }

    /**
     * 真流式模式执行（大文件 &ge; 1万行）
     * <p>与全量模式的核心差异：不持有全量 List，每解析一批立即处理一批并释放内存。</p>
     *
     * <p><b>流程：</b></p>
     * <ol>
     *   <li>创建聚合根并启动任务（total 使用预估值）</li>
     *   <li>启动流式解析：每攒够 batchSize 条触发一次回调</li>
     *   <li>回调内直接完成：校验 → 转换 → 持久化 → 更新进度</li>
     *   <li>回调结束后该批次数据可被 GC，内存占用恒定</li>
     *   <li>全部解析完成后标记最终状态</li>
     * </ol>
     *
     * @param taskId 任务唯一标识
     * @param file 临时 Excel 文件
     * @param plugin 业务插件
     * @param batchSize 每批处理条数（由插件决定）
     * @param estimatedRows 预估总行数（用于初始化进度，实际以处理为准）
     * @param <T> Excel 解析对应的 DTO 类型
     * @param <E> 持久化对应的 Entity 类型
     */
    private <T, E> void doExecuteStream(
        String taskId, File file, ImportPlugin<T, E> plugin, int batchSize, int estimatedRows) {

        ErrorCollectorHolder.remove();
        ErrorCollector collector = ErrorCollectorHolder.get();

        // 1. 创建聚合根并启动任务（total 使用预估值）
        ImportTask task = new ImportTask(taskId);
        task.start(estimatedRows);
        importTaskRepository.save(task);

        // 2. 流式状态跟踪：使用原子类保证回调内的线程安全
        AtomicInteger batchIndex = new AtomicInteger(0);

        // 3. 启动流式解析：Consumer 回调中直接处理，不长期持有引用
        fileParsingPort.parseStream(file, plugin.getDtoClass(), batchSize, batch -> {
            // 每批处理前检查取消
            checkCancelled(task, taskId);

            int currentBatch = batchIndex.incrementAndGet();

            // 3.1 处理当前批次
            BatchResult result = processBatch(taskId, batch, plugin, currentBatch);

            List<String> latestSummary  = buildErrorSummary(collector);
            // 3.2 实时上报进度
            task.recordBatch(result.successIncrement, result.failIncrement, latestSummary);
            importTaskRepository.save(task);
        });

        // 4. 流式解析结束，汇总最终结果
        if (collector.hasErrors()) {
            saveErrorFile(taskId, collector, plugin);
        }

        if (task.getFailCount() == 0) {
            task.finishSuccess();
        } else {
            task.finishPartial();
        }
        importTaskRepository.save(task);

        log.info("[{}] Stream import completed, success={}, fail={}",
            taskId, task.getSuccessCount(), task.getFailCount());

        ErrorCollectorHolder.remove();
    }

    /**
     * 处理单批数据：校验 → 转换 → 持久化
     * <p>提取公共逻辑，避免全量/流式两个方法代码重复</p>
     * <p>全量模式和流式模式复用的核心逻辑，确保两种模式行为一致。</p>
     * <p>异常策略：本批次任何环节抛异常，整批标记为失败，不影响其他批次。</p>
     *
     * @param taskId 任务 ID，用于日志串联
     * @param batch 当前批次原始数据（Excel 解析后的 DTO 列表）
     * @param plugin 业务插件，提供 validate / convert / persist 实现
     * @param batchNo 当前批次序号（从 1 开始），用于错误定位
     * @return 批次处理结果（成功增量、失败增量）
     * @param <T> Excel 解析对应的 DTO 类型
     * @param <E> 持久化对应的 Entity 类型
     */
    private <T, E> BatchResult processBatch(
        String taskId, List<T> batch, ImportPlugin<T, E> plugin, int batchNo) {

        ErrorCollector collector = ErrorCollectorHolder.get();
        int batchSize = batch.size();

        try {
            // 1. 业务校验：插件过滤非法/重复数据
            List<T> validated = plugin.validate(batch);
            int filtered = batch.size() - validated.size();

            if (filtered > 0) {
                log.debug("[{}] Batch {} filtered {} invalid rows", taskId, batchNo, filtered);
            }

            // 2. 防御：整批校验不通过时直接标记失败，跳过转换和持久化
            if (validated.isEmpty()) {
                // 只添加当前批次特有的错误，不遍历collector
                collector.addError(-1, "批次" + batchNo + "全部校验失败");
                return new BatchResult(0, batch.size());
            }

            // 3. 数据转换：DTO → Domain → Entity（含密码加密、默认值填充等）
            List<E> entities = plugin.convert(validated);

            // 4. 批量持久化：写入数据库（插件内部可再分片，防止 SQL 过长）
            int inserted = plugin.persist(entities);

            // 5. 计算失败数 = 总行数 - 成功数
            //    所有失败的行都已经在 collector 中有记录，计数只是为了统计和进度展示
            int failed = batchSize - inserted;

            log.debug("[{}] Batch {} processed successfully, success={}, fail={}",
                taskId, batchNo, inserted, failed);

            return new BatchResult(inserted, failed);

        } catch (Exception e) {
            // 故障隔离：单批失败只影响本批次，记录错误后继续处理下一批
            log.warn("[{}] Batch {} processing failed", taskId, batchNo, e);
            collector.addError(-1,
                "Batch " + batchNo + " processing exception: " + e.getMessage());
            return new BatchResult(0, batch.size());
        }

    }

    // ==================== 辅助方法 ====================

    /**
     * 检查任务是否被取消。
     * <p>
     * 若已取消，则标记聚合根为 CANCELLED、持久化，并抛出 {@link ImportCancelledException}。
     * </p>
     *
     * @param task   当前任务聚合根
     * @param taskId 任务 ID（用于查询取消标记）
     */
    private void checkCancelled(ImportTask task, String taskId) {
        if (importTaskRepository.isCancelled(taskId)) {
            task.cancel();
            importTaskRepository.save(task);
            throw new ImportCancelledException(ResultCode.TASK_CANCELLED, "Task cancelled");
        }
    }

    /**
     * 单批处理结果封装
     * <p>避免使用 Map 或数组传递两个 int，语义更清晰，享受编译期类型检查。</p>
     *
     * @param successIncrement 本批次成功条数
     * @param failIncrement 本批次失败条数（含校验过滤和异常）
     */
    private record BatchResult(int successIncrement, int failIncrement) {}

    /**
     * 保存错误文件并记录路径到聚合根。
     *
     * @param taskId    任务 ID
     * @param collector 错误信息收集器
     * @param plugin    业务插件
     */
    private void saveErrorFile(String taskId, ErrorCollector collector, ImportPlugin<?, ?> plugin) {
        try {
            File errorFile = errorFileGenerator.generateErrorFile(
                collector.getErrors(),
                plugin.getHeaders()
            );
            importTaskRepository.findById(taskId).ifPresent(task -> {
                task.setErrorFilePath(errorFile.getAbsolutePath());
                importTaskRepository.save(task);
            });
            log.info("[{}] Error file saved: {}", taskId, errorFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("[{}] Failed to save error file", taskId, e);
        }
    }

    /**
     * 标记任务失败并持久化（幂等，收口自 catch 块）。
     * <p>
     * 任务已存在则迁移到 FAILED；不存在（如初始创建失败）则新建后标记。
     * 持久化本身失败（如 Redis 不可用，此时 {@link ImportTaskRepository#save}
     * 会因首次创建而抛异常）只记录 ERROR 日志，不再向外抛出，避免掩盖原始异常
     * 或在 catch 块内再次抛异常导致 finally 无法兜底。
     * </p>
     *
     * @param taskId   任务 ID
     * @param errorMsg 失败原因（面向客户的中文消息）
     */
    private void markTaskFailed(String taskId, String errorMsg) {
        try {
            importTaskRepository.findById(taskId)
                .ifPresentOrElse(
                    task -> {
                        task.fail(errorMsg);
                        importTaskRepository.save(task);
                    },
                    () -> {
                        ImportTask task = new ImportTask(taskId);
                        task.fail(errorMsg);
                        importTaskRepository.save(task);
                    }
                );
        } catch (Exception e) {
            log.error("[{}] Failed to persist FAILED state: {}", taskId, e.getMessage(), e);
        }
    }

    /**
     * 从 ErrorCollector 构建错误摘要（用于前端轮询显示）
     *
     * @param collector 错误收集器
     * @return 格式化后的错误摘要列表（最多 ERROR_MSG_MAX_COUNT 条，已去重）
     */
    private List<String> buildErrorSummary(ErrorCollector collector) {
        if (collector == null || !collector.hasErrors()) {
            return List.of();
        }

        Set<String> uniqueErrors = new LinkedHashSet<>();

        for (ErrorRecord error : collector.getErrors()) {
            String msg = (error.getRowIndex() > 0
                ? "第" + error.getRowIndex() + "行" : "未知行号")
                + "：" + error.getErrorReason();
            uniqueErrors.add(msg);
            if (uniqueErrors.size() >= appProperties.getDisplay().getErrorMsgMaxCount()) {
                break;
            }
        }

        return new ArrayList<>(uniqueErrors);
    }

}
