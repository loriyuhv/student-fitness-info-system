package com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.redis.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Redis Hash Field 常量（导入任务）
 * <p>杜绝 "status"、"total" 等字符串硬编码，享受 IDE 自动补全与编译期检查</p>
 * @author loriyuhv
 * @version 1.0 2026/8/22 11:38
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum ImportTaskField {
    STATUS("status"),
    TOTAL("total"),
    PROCESSED("processed"),
    SUCCESS_COUNT("successCount"),
    FAIL_COUNT("failCount"),
    ERROR_MSG("errorMsg");

    private final String key;

    @Override
    public String toString() {
        return key;
    }
}
