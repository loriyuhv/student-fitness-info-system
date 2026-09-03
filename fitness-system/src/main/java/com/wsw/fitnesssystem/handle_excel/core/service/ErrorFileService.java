package com.wsw.fitnesssystem.handle_excel.core.service;

import cn.idev.excel.FastExcel;
import com.wsw.fitnesssystem.handle_excel.core.model.ErrorRecord;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/29 20:05
 * @since 1.0
 */
@Slf4j
@Service
public class ErrorFileService {

    /**
     * 生成错误 Excel 文件
     */
    public File generateErrorFile(List<ErrorRecord> errors, List<String> headers) {
        if (errors == null || errors.isEmpty()) {
            throw new IllegalArgumentException("错误列表为空");
        }

        // 列头：行号 + 原始列头 + 错误原因
        List<String> newHeaders = new ArrayList<>();
        newHeaders.add("行号");
        newHeaders.addAll(headers);
        newHeaders.add("错误原因");

        // 数据行
        List<List<String>> dataRows = buildErrorDataRows(errors, headers);

        try {
            File tempFile = File.createTempFile("import_errors_", ".xlsx");
            FastExcel.write(tempFile)
                .head(newHeaders.stream().map(List::of).toList())
                .sheet("错误数据")
                .doWrite(dataRows);
            log.info("错误文件生成成功: {}", tempFile.getAbsolutePath());
            return tempFile;
        } catch (Exception e) {
            log.error("生成错误 Excel 失败", e);
            throw new RuntimeException("生成错误文件失败", e);
        }
    }

    private static @NonNull List<List<String>> buildErrorDataRows(List<ErrorRecord> errors, List<String> headers) {
        List<List<String>> dataRows = new ArrayList<>();

        for (ErrorRecord error : errors) {
            ArrayList<String> row = new ArrayList<>();

            // 1. 行号列（如果 rowIndex > 0 则显示，否则显示 "未知"）
            row.add(error.getRowIndex() > 0 ? String.valueOf(error.getRowIndex()) : "未知");

            // 2. 原始数据列
            ArrayList<String> rowData = new ArrayList<>(error.getRowData());
            while (rowData.size() < headers.size()) {
                rowData.add(""); // 补齐空列
            }
            row.addAll(rowData);

            // 3. 错误原因
            row.add(error.getErrorReason());

            dataRows.add(row);
        }
        return dataRows;
    }

}
