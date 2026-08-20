package com.wsw.fitnesssystem.handle_excel.interfaces.dto;

import lombok.Data;

import java.util.Map;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/26 16:09
 * @since 1.0
 */
@Data
public class ImportProgressDTO {
    private int total;
    private int processed;
    private int successCount;
    private int failCount;
    private String status;
    private String errorMsg;

    public static ImportProgressDTO from(Map<Object, Object> map) {
        ImportProgressDTO dto = new ImportProgressDTO();
        dto.setTotal(parseInt(map.get("total")));
        dto.setProcessed(parseInt(map.get("processed")));
        dto.setSuccessCount(parseInt(map.get("successCount")));
        dto.setFailCount(parseInt(map.get("failCount")));
        dto.setStatus((String) map.getOrDefault("status", "INIT"));
        dto.setErrorMsg((String) map.getOrDefault("errorMsg", ""));
        return dto;
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
