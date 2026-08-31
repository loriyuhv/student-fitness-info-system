package com.wsw.fitnesssystem.handle_excel.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

/**
 * Excel 模板领域模型
 * <p>Core 层业务对象，与数据库实体解耦</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/1 03:45
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportTemplate {

    /** 业务类型 */
    private String bizType;

    /** 模板文件名 */
    private String fileName;

    /** Sheet 名称 */
    private String sheetName;

    /** 列头 */
    private List<String> headers;

    /** 列规则说明 */
    private List<String> rules;

    /** 示例数据 */
    private List<List<String>> examples;

    /**
     * 获取表头行
     */
    public List<List<String>> getHeadRows() {
        if (headers == null) {
            return List.of();
        }

        return headers.stream().map(List::of).toList();
    }

    /**
     * 获取数据行（列说明 + 示例数据）
     */
    public List<List<String>> getDataRows() {
        return Stream.concat(
            rules != null ? Stream.of(rules) : Stream.empty(),
            examples != null ? examples.stream() : Stream.empty()
        ).toList();
    }

    /**
     * 校验模板是否有效
     */
    public boolean isValid() {
        return headers != null && !headers.isEmpty()
            && rules != null && !rules.isEmpty()
            && fileName != null && !fileName.isBlank();
    }

}
