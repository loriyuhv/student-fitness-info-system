package com.wsw.fitnesssystem.handle_excel.interfaces.dto;

import lombok.Data;

import java.util.Map;

/**
 * 导入进度 DTO
 * 用于前端展示导入状态
 *
 * @author loriyuhv
 * @version 1.0 2026/3/26 16:09
 * @since 1.0
 */
@Data
public class ImportProgressDTO {
    /**
     * 总数据条数
     */
    private int total;
    /**
     * 已处理条数（成功 + 失败）
     */
    private int processed;
    /**
     * 成功条数
     */
    private int successCount;
    /**
     * 失败条数
     */
    private int failCount;
    /**
     * 状态：INIT / PROCESSING / FINISHED / PARTIAL / FAILED / NOT_FOUND
     */
    private String status;
    /**
     * 错误信息摘要（最多前3条）
     */
    private String errorMsg;

    /**
     * 从 Redis Hash 转换为 DTO
     * @param map map
     * @return DTO
     */
    public static ImportProgressDTO from(Map<Object, Object> map) {
        if (map == null || map.isEmpty()) {
            ImportProgressDTO empty = new ImportProgressDTO();
            empty.setStatus("NOT_FOUND");
            return empty;
        }

        ImportProgressDTO dto = new ImportProgressDTO();
        dto.setTotal(parseInt(map.get("total")));
        dto.setProcessed(parseInt(map.get("processed")));
        dto.setSuccessCount(parseInt(map.get("successCount")));
        dto.setFailCount(parseInt(map.get("failCount")));
        dto.setStatus((String) map.getOrDefault("status", "INIT"));
        dto.setErrorMsg((String) map.getOrDefault("errorMsg", ""));
        return dto;
    }

    /**
     * 计算进度百分比
     * @return 百分比
     */
    public int getPercent() {
        if (total <= 0) return 0;
        return Math.min(100, (int) ((processed * 100.0) / total));
    }

    /**
     * 是否已完成（成功或部分成功）
     * @return 标志
     */
    public boolean isCompleted() {
        return "FINISHED".equals(status) || "PARTIAL".equals(status);
    }

    /**
     * 是否失败
     * @return 失败
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    private static int parseInt(Object val) {
        if (val == null) return 0;
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
