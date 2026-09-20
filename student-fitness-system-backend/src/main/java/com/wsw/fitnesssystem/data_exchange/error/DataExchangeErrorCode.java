package com.wsw.fitnesssystem.data_exchange.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;

/**
 * data_exchange 模块错误码（文件导入导出）。
 *
 * <p><b>职责：</b>承载文件导入导出任务生命周期、批量处理相关的错误码。</p>
 *
 * <p><b>覆盖范围：</b></p>
 * <ul>
 *   <li><b>任务状态：</b>导入任务不存在、导入任务已被取消</li>
 * </ul>
 *
 * <p><b>与 {@code CommonErrorCode} 的边界：</b></p>
 * <ul>
 *   <li>通用文件错误（{@code common.file.*}）→ {@code CommonErrorCode}</li>
 *   <li>业务级任务错误（{@code data_exchange.*}）→ 本枚举</li>
 * </ul>
 *
 * <p><b>命名规范：</b>{@code data_exchange.*}</p>
 *
 * <p><b>HTTP 映射：</b>由接口层 {@code HttpStatusResolver} 显式注册，
 * 本枚举不感知 HTTP 语义。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 18:35
 * @since 1.0
 */
public enum DataExchangeErrorCode implements ErrorCode {

    /** 导入任务不存在 */
    IMPORT_TASK_NOT_FOUND("data_exchange.import_task.not_found", "导入任务不存在"),

    /** 导入任务已被用户取消 */
    TASK_CANCELLED("data_exchange.task.cancelled", "导入任务已被用户取消");

    private final String code;
    private final String message;

    DataExchangeErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

}
