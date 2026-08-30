package com.wsw.fitnesssystem.handle_excel.core.collector;

import com.wsw.fitnesssystem.handle_excel.core.model.ErrorRecord;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 错误信息收集器（线程不安全，仅供单线程使用）
 *
 * @author loriyuhv
 * @version 1.0 2026/8/29 15:25
 * @since 1.0
 */
@Getter
public class ErrorCollector {

    private final List<ErrorRecord> errors = new ArrayList<>();

    /**
     * 添加错误记录
     * @param rowIndex Excel 行号（从 1 开始）
     * @param rowData  该行的数据（字符串列表）
     * @param reason   错误原因
     */
    public void addError(int rowIndex, List<String> rowData, String reason) {
        errors.add(new ErrorRecord(rowIndex, rowData, reason));
    }

    public void addError(int rowIndex, String reason) {
        errors.add(new ErrorRecord(rowIndex, List.of(), reason));
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public int getErrorCount() {
        return errors.size();
    }

}
