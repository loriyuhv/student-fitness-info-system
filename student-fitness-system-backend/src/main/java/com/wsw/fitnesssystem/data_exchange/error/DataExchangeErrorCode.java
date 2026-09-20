package com.wsw.fitnesssystem.data_exchange.error;

import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;

/**
 * data_exchange 模块错误码（文件导入导出）。
 *
 * <p><b>命名规范：</b>{@code data_exchange.*}。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 18:35
 * @since 1.0
 */
public enum DataExchangeErrorCode implements ErrorCode {

    IMPORT_TASK_NOT_FOUND("data_exchange.import_task.not_found", "导入任务不存在"),
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
