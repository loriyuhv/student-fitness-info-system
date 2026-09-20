package com.wsw.fitnesssystem.data_exchange.application.exception;

/**
 * 导入任务被用户主动取消的<strong>控制流异常</strong>。
 *
 * <p><b>设计定位：</b>本异常<strong>不是「错误」</strong>，而是中断解析流程的信号。
 * 它只在 {@code ImportOrchestrator} 内部被捕获并终止流程，
 * <strong>不经过</strong> {@code GlobalExceptionHandler}，<strong>不返回</strong>给前端。</p>
 *
 * <p><b>为什么继承 {@link RuntimeException} 而不是 {@code BizException}？</b></p>
 * <ul>
 *   <li>任务取消是「用户主动行为」，不是「业务规则失败」，语义上不是错误</li>
 *   <li>不携带 {@code ErrorCode}，不参与错误码体系</li>
 *   <li>不期望被 {@code GlobalExceptionHandler} 处理，避免误返回 4xx 给前端</li>
 *   <li>与业务异常分离，避免上层 {@code catch (BizException)} 时误捕</li>
 * </ul>
 *
 * <p><b>抛出位置：</b>{@code ImportOrchestrator.checkCancelled()} 检测到取消标记时。</p>
 *
 * <p><b>捕获位置：</b></p>
 * <ul>
 *   <li>{@code ExcelFileParser}：捕获后透传（避免被通用 {@code catch (Exception)} 吞掉）</li>
 *   <li>{@code ImportOrchestrator.execute()}：捕获后 log.warn 并终止流程</li>
 * </ul>
 *
 * <p><b>与领域方法的边界：</b>任务状态迁移（RUNNING → CANCELLED）由领域方法
 * {@code ImportTask.cancel()} 完成；本异常只负责「中断解析流程」这一技术目标。
 * 二者职责分离。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21 02:25
 * @since 1.0
 */
public class ImportCancelledException extends RuntimeException {

    /**
     * 使用取消原因构造异常。
     *
     * @param message 取消原因（用于日志排查，不面向终端用户）
     */
    public ImportCancelledException(String message) {
        super(message);
    }

}
