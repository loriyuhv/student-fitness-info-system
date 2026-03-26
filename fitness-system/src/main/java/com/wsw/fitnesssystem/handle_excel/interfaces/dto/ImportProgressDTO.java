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
    private String status;

    public static ImportProgressDTO from(Map<Object, Object> map) {
        ImportProgressDTO dto = new ImportProgressDTO();

        dto.setTotal(Integer.parseInt((String) map.getOrDefault("total", "0")));
        dto.setProcessed(Integer.parseInt((String) map.getOrDefault("processed", "0")));
        dto.setStatus((String) map.getOrDefault("status", "INIT"));

        return dto;
    }
}
