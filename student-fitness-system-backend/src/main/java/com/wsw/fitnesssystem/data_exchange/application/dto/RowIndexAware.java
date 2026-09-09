package com.wsw.fitnesssystem.data_exchange.application.dto;

/**
 * 行号感知接口。
 * <p>
 * 由需要接收文件行号的 DTO 实现，供解析器在读取文件时注入行号。
 * </p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/31 15:56
 * @since 1.0
 */
public interface RowIndexAware {

    void setRowIndex(Integer rowIndex);

    Integer getRowIndex();

}
