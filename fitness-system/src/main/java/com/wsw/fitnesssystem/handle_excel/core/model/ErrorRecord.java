package com.wsw.fitnesssystem.handle_excel.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Excel 导入错误记录
 * 包含行号、原始行数据、错误原因
 *
 * @author loriyuhv
 * @version 1.0 2026/8/29 15:23
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorRecord {

    /** Excel 行号（从 1 开始，包含表头行则从 2 开始） */
    private int rowIndex;

    /** 原始行数据（对应 Excel 列值） */
    private List<String> rowData;

    /** 错误原因描述 */
    private String errorReason;

}
