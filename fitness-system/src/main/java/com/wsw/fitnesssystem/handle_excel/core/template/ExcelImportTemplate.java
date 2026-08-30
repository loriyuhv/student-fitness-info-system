package com.wsw.fitnesssystem.handle_excel.core.template;

import com.google.common.collect.Lists;
import com.wsw.fitnesssystem.handle_excel.core.adapter.ImportAdapter;
import com.wsw.fitnesssystem.handle_excel.core.collector.ErrorCollector;
import com.wsw.fitnesssystem.handle_excel.core.collector.ErrorCollectorHolder;
import com.wsw.fitnesssystem.handle_excel.core.exception.ExcelException;
import com.wsw.fitnesssystem.handle_excel.core.model.ErrorRecord;
import com.wsw.fitnesssystem.handle_excel.core.parser.ExcelParser;
import com.wsw.fitnesssystem.handle_excel.core.port.ImportProgressPort;
import com.wsw.fitnesssystem.handle_excel.core.service.ErrorFileService;
import com.wsw.fitnesssystem.handle_excel.core.utils.FileCleanupUtils;
import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ExcelConstants;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <b>Excel 导入模板方法</b>
 * <p>定义标准导入流程，与具体业务完全解耦。所有业务导入只需实现 {@link ImportAdapter} 即可接入。</p>
 *
 * <p><b>核心设计：</b></p>
 * <ol>
 *     <li>双模式解析：小文件走全量（简单高效），大文件走真流式（内存安全）</li>
 *     <li>批处理抽象：全量/流式两种模式复用同一套 {@link #processBatch} 逻辑</li>
 *     <li>故障隔离：单批失败不影响其他批次，最终状态为 PARTIAL</li>
 *     <li>资源兜底：finally 强制清理临时文件，防止磁盘泄漏</li>
 * </ol>
 *
 * <p><b>标准流程：</b></p>
 * <ol>
 *     <li>预估行数，智能选择全量/流式解析 Excel（通用）</li>
 *     <li>业务校验（适配器实现）</li>
 *     <li>数据转换（适配器实现）</li>
 *     <li>批量持久化（适配器实现）</li>
 *     <li>上报进度（通用）</li>
 *     <li>完成/异常处理（通用）</li>
 * </ol>
 *
 * <p><b>内存安全策略：</b></p>
 * <ol>
 *     <li>小文件（&lt; 1万行）：全量解析，代码简单，内存可控</li>
 *     <li>大文件（&ge; 1万行）：真流式解析，每批处理完立即释放，内存占用 = O(batchSize)</li>
 * </ol>
 *
 * <p><b>异常处理策略：</b></p>
 * <ol>
 *     <li>解析异常：任务终止，locked=FAILED</li>
 *     <li>单批异常：故障隔离，继续下一批，locked=PARTIAL</li>
 *     <li>全局异常：任务终止，locked=FAILED</li>
 *     <li>临时文件：finally 强制清理</li>
 * </ol>
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
    private final ErrorFileService errorFileService;
    private final ImportProgressPort importProgressPort;

    /**
     * 执行导入（模板方法）
     * <p><b>完整流程：</b></p>
     * <ol>
     *   <li>预估 Excel 数据行数，智能选择全量/流式模式</li>
     *   <li>进入对应执行分支（{@link #doExecuteFull} 或 {@link #doExecuteStream}）</li>
     *   <li>分支内部：解析 → 校验 → 转换 → 持久化 → 上报进度</li>
     *   <li>最终状态判定：全部成功 = FINISHED，部分失败 = PARTIAL</li>
     *   <li>finally 强制清理临时文件</li>
     * </ol>
     *
     * @param taskId 任务唯一标识，用于进度追踪与日志串联
     * @param file 已转存到磁盘的临时 Excel 文件（非 MultipartFile，避免 InputStream 异步关闭）
     * @param adapter 业务适配器，封装了具体业务的校验/转换/持久化逻辑
     * @param <T> Excel 解析对应的 DTO 类型
     * @param <E> 持久化对应的 Entity 类型
     */
    public <T, E> void execute(String taskId, File file, ImportAdapter<T, E> adapter) {

        try {
            // ========== Step 1: 预估行数，决策解析模式 ==========
            int estimatedRows = excelParser.estimatedRowCount(file);
            int batchSize = adapter.getBatchSize();
            if (estimatedRows < ExcelConstants.STREAM_THRESHOLD) {
                // 小文件：全量解析，代码简单，内存 = O(total)
                log.info("[{}] Estimated {} rows, using full processing mode", taskId, estimatedRows);
                doExecuteFull(taskId, file, adapter);
            } else {
                // 大文件：真流式解析，内存 = O(batchSize)，与文件大小无关
                log.info("[{}] Estimated {} rows, using streaming processing mode, batchSize={}",
                    taskId, estimatedRows, batchSize);
                doExecuteStream(taskId, file, adapter, batchSize, estimatedRows);
            }
        } catch (ExcelException e) {
            // Excel 模块已知异常（格式损坏、密码保护、解析失败等）
            String defaultMsg = e.getResultCode().getMessage();
            String customMsg = e.getMessage();
            String finalMsg = defaultMsg + "：" + customMsg;
            log.error("[{}] Business exception occurred: {}", taskId, finalMsg, e);
            importProgressPort.fail(taskId, finalMsg);
        } catch (Exception e) {
            // 未知异常兜底：防止任何未捕获异常导致任务状态悬空
            log.error("[{}] Import task terminated abnormally", taskId, e);
            importProgressPort.fail(taskId, ResultCode.SERVER_TEMP_ERROR.getMessage());
        } finally {
            // ========== Step 5: 清理临时文件（强制兜底） ==========
            FileCleanupUtils.cleanup(file);
        }

    }

    /**
     * 全量模式执行（小文件 &lt; 1万行）
     *
     * <p><b>流程：</b></p>
     * <ol>
     *   <li>一次性解析完整 Excel 到 List</li>
     *   <li>空文件校验：直接标记 FAILED 并返回</li>
     *   <li>Redis 初始化进度（locked = PROCESSING）</li>
     *   <li>按 batchSize 分片为若干批次</li>
     *   <li>逐批调用 {@link #processBatch} 处理</li>
     *   <li>每批结束后更新 Redis 进度</li>
     *   <li>全部批次完成后标记最终状态</li>
     * </ol>
     *
     * @param taskId 任务唯一标识
     * @param file 临时 Excel 文件
     * @param adapter 业务适配器
     * @param <T> Excel 解析对应的 DTO 类型
     * @param <E> 持久化对应的 Entity 类型
     */
    private <T, E> void doExecuteFull(
            String taskId, File file, ImportAdapter<T, E> adapter) {

        // 1. 全量解析：一次性读入内存，适合小文件
        List<T> list = excelParser.parseFull(file, adapter.getDtoClass(), taskId);
        int total = list.size();

        // 2. 空文件防御：无可解析数据时直接失败，避免无意义轮询
        if (total == 0) {
            log.warn("[{}] Excel file is empty or no data to parse", taskId);
            importProgressPort.fail(taskId, "Excel file is empty or no data to parse");
            return;
        }

        // 3. 初始化 Redis 进度：客户端可立即查询到 total 和 PROCESSING 状态
        importProgressPort.init(taskId, total);

        // 4. 初始化全局错误收集器（懒加载，自动创建）
        ErrorCollectorHolder.remove(); // 清理旧数据（防御性）
        ErrorCollector collector = ErrorCollectorHolder.get(); // 懒加载创建

        // 5. 分片：将全量 List 切分为固定大小的批次
        int batchSize = adapter.getBatchSize();
        List<List<T>> batches = Lists.partition(list, batchSize);
        log.info("[{}] 分片完成，共 {} 批，每批 {} 条", taskId, batches.size(), batchSize);

        // 6. 逐批处理：累加成功/失败计数
        int successCount = 0;
        int failCount = 0;
        List<String> errorMsgList = new ArrayList<>();

        for (int i = 0; i < batches.size(); i++) {
            List<T> batch = batches.get(i);

            // 6.1 处理单批：校验 → 转换 → 持久化
            BatchResult result = processBatch(taskId, batch, adapter, i + 1, errorMsgList);
            successCount += result.successIncrement;
            failCount += result.failIncrement;

            // 6.2 实时上报进度：客户端轮询可感知到处理进展
            importProgressPort.updateProgress(taskId, successCount, failCount, errorMsgList);
        }

        // 6. 最终状态判定 + 错误文件
        if (collector.hasErrors()) {
            saveErrorFile(taskId, collector, adapter);
        }

        if (failCount == 0) {
            // 全部成功
            importProgressPort.finish(taskId, successCount);
            log.info("[{}] 导入任务全部成功完成, total={}, success={}", taskId, total, successCount);
        } else {
            // 部分成功（存在失败批次或校验过滤）
            importProgressPort.partial(taskId, successCount, failCount, errorMsgList);
            log.info("[{}] 导入任务部分完成, total={}, success={}, fail={}",
                    taskId, total, successCount, failCount);
        }

        // 7. 清理 ThreadLocal
        ErrorCollectorHolder.remove();

    }

    /**
     * 真流式模式执行（大文件 &ge; 1万行）
     * <p>与全量模式的核心差异：不持有全量 List，每解析一批立即处理一批并释放内存。</p>
     *
     * <p><b>流程：</b></p>
     * <ol>
     *   <li>Redis 初始化进度（此时 total 为预估值，非精确值）</li>
     *   <li>启动流式解析：EasyExcel 每攒够 batchSize 条触发一次回调</li>
     *   <li>回调内直接完成：校验 → 转换 → 持久化 → 更新进度</li>
     *   <li>回调结束后该批次数据可被 GC，内存占用恒定</li>
     *   <li>全部解析完成后标记最终状态</li>
     * </ol>
     *
     * @param taskId 任务唯一标识
     * @param file 临时 Excel 文件
     * @param adapter 业务适配器
     * @param batchSize 每批处理条数（由适配器决定）
     * @param estimatedRows 预估总行数（用于初始化进度，实际以处理为准）
     * @param <T> Excel 解析对应的 DTO 类型
     * @param <E> 持久化对应的 Entity 类型
     */
    private <T, E> void doExecuteStream(
            String taskId, File file, ImportAdapter<T, E> adapter,
            int batchSize, int estimatedRows) {
        ErrorCollectorHolder.remove();
        ErrorCollector collector = ErrorCollectorHolder.get();

        // 1. 初始化进度：total 使用预估值，processed 会从 0 开始累加
        importProgressPort.init(taskId, estimatedRows);

        // 2. 流式状态跟踪：使用原子类保证回调内的线程安全
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> errorMsgList = new ArrayList<>();
        AtomicInteger batchIndex = new AtomicInteger(0);

        // 3. 启动流式解析：Consumer 回调中直接处理，不长期持有引用
        excelParser.parseStream(file, adapter.getDtoClass(), batchSize, batch -> {
            int currentBatch = batchIndex.incrementAndGet();

            // 3.1 处理当前批次
            BatchResult result = processBatch(taskId, batch, adapter, currentBatch, errorMsgList);
            successCount.addAndGet(result.successIncrement);
            failCount.addAndGet(result.failIncrement);

            // 3.2 实时上报进度
            importProgressPort.updateProgress(taskId, successCount.get(), failCount.get(), errorMsgList);
        });

        // 4. 流式解析结束，汇总最终结果
        int finalSuccess = successCount.get();
        int finalFail = failCount.get();

        if (collector.hasErrors()) {
            saveErrorFile(taskId, collector, adapter);
        }

        if (finalFail == 0) {
            importProgressPort.finish(taskId, finalSuccess);
            log.info("[{}] 流式导入任务全部成功完成, success={}", taskId, finalSuccess);
        } else {
            importProgressPort.partial(taskId, finalSuccess, finalFail, errorMsgList);
            log.info("[{}] 流式导入任务部分完成, success={}, fail={}",
                    taskId, finalSuccess, finalFail);
        }

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
     * @param adapter 业务适配器，提供 validate / convert / persist 实现
     * @param batchNo 当前批次序号（从 1 开始），用于错误定位
     * @param errorMsgList 全局错误信息列表（本方法向其中追加错误，受 MAX_COUNT 限制）
     * @return 批次处理结果（成功增量、失败增量）
     * @param <T> Excel 解析对应的 DTO 类型
     * @param <E> 持久化对应的 Entity 类型
     */
    private <T, E> BatchResult processBatch(
            String taskId, List<T> batch,
            ImportAdapter<T, E> adapter, int batchNo, List<String> errorMsgList) {

        ErrorCollector collector = ErrorCollectorHolder.get(); // 获取全局唯一实例

        try {
            // 3.1 业务校验：适配器过滤非法/重复数据
            List<T> validated = adapter.validate(batch);
            int filtered = batch.size() - validated.size();

            if (filtered > 0) {
                log.info("[{}] 第 {} 批过滤 {} 条重复/非法数据", taskId, batchNo, filtered);
            }

            // 3.1.1 防御：整批校验不通过时直接标记失败，跳过转换和持久化
            if (validated.isEmpty()) {
                addErrorMsg(errorMsgList, "第" + batchNo + "批数据全部校验失败");
                collector.addError(-1, "批次" + batchNo + "全部校验失败");
                return new BatchResult(0, batch.size());
            }

            // 3.2 数据转换：DTO → Domain → Entity（含密码加密、默认值填充等）
            List<E> entities = adapter.convert(validated);

            // 3.3 批量持久化：写入数据库（适配器内部可再分片，防止 SQL 过长）
            adapter.persist(entities);

            // 收集错误信息到前端展示
            if (collector.hasErrors()) {
                for (ErrorRecord error : collector.getErrors()) {
                    String msg = (error.getRowIndex() > 0 ? "第" + error.getRowIndex() + "行" : "批次" + batchNo)
                        + ": " + error.getErrorReason();
                    addErrorMsg(errorMsgList, msg);
                }
            }

            log.info("[{}] 第 {} 批处理完成，success={}, fail={}",
                    taskId, batchNo, validated.size(), filtered);

            return new BatchResult(validated.size(), filtered);

        } catch (Exception e) {
            // 故障隔离：单批失败只影响本批次，记录错误后继续处理下一批
            log.error("[{}] 第 {} 批处理失败, batchSize={}", taskId, batchNo, batch.size(), e);
            addErrorMsg(errorMsgList, "第" + batchNo + "批:" + truncate(e.getMessage()));
            collector.addError(-1, "批次" + batchNo + "处理异常: " + e.getMessage());
            return new BatchResult(0, batch.size());
        }

    }

    // ========== 错误文件保存 ==========
    private void saveErrorFile(String taskId, ErrorCollector collector, ImportAdapter<?, ?> adapter) {
        try {
            File errorFile = errorFileService.generateErrorFile(
                collector.getErrors(),
                adapter.getHeaders()
            );
            importProgressPort.saveErrorFilePath(taskId, errorFile.getAbsolutePath());
            log.info("[{}] 错误文件已保存: {}", taskId, errorFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("[{}] 保存错误文件失败", taskId, e);
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
