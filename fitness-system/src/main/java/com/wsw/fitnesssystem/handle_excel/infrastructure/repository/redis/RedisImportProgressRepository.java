package com.wsw.fitnesssystem.handle_excel.infrastructure.repository.redis;

import com.wsw.fitnesssystem.handle_excel.core.port.ImportProgressPort;
import com.wsw.fitnesssystem.handle_excel.domain.enums.ImportStatus;
import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ExcelConstants;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.redis.model.ExcelRedisKeys;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.redis.model.ImportTaskField;
import com.wsw.fitnesssystem.handle_excel.interfaces.dto.ImportProgressDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导入进度 Redis 实现
 * <p>基于 Redis Hash 存储任务进度，支持 24h 自动过期</p>
 * <p>Key 规范：excel:import:task:{taskId}</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 14:17
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisImportProgressRepository implements ImportProgressPort {

    private final StringRedisTemplate redis;
    private static final String ERROR_FILE_FIELD = "errorFilePath";

    /**
     * 初始化进度（解析完 Excel 后调用）
     * @param taskId 任务ID
     * @param total 总数据条数
     */
    public void init(String taskId, int total) {
        Map<String, String> map = new HashMap<>();
        put(map, ImportTaskField.STATUS, ImportStatus.PROCESSING.name());
        put(map, ImportTaskField.TOTAL, total);
        put(map, ImportTaskField.PROCESSED, 0);
        put(map, ImportTaskField.SUCCESS_COUNT, 0);
        put(map, ImportTaskField.FAIL_COUNT, 0);
        put(map, ImportTaskField.ERROR_MSG, "");

        String key = ExcelRedisKeys.importTaskKey(taskId);
        try {
            redis.opsForHash().putAll(key, map);
            redis.expire(key, Duration.ofHours(ExcelConstants.IMPORT_TASK_TTL_HOURS));
            log.info("[{}] 进度初始化完成, total={}", taskId, total);
        } catch (Exception e) {
            log.error("[{}] Redis 进度初始化失败", taskId, e);
        }
    }

    /**
     * 更新进度（每批处理完调用）
     *
     * @param taskId 任务ID
     * @param successCount 成功数量
     * @param failCount 失败数量
     * @param errorMsgList 错误信息列表
     */
    public void updateProgress(String taskId, int successCount, int failCount,
                               List<String> errorMsgList) {
        Map<String, String> map = new HashMap<>();
        put(map, ImportTaskField.PROCESSED, successCount + failCount);
        put(map, ImportTaskField.SUCCESS_COUNT, successCount);
        put(map, ImportTaskField.FAIL_COUNT, failCount);
        put(map, ImportTaskField.ERROR_MSG, errorMsgList);

        try {
            redis.opsForHash().putAll(ExcelRedisKeys.importTaskKey(taskId), map);
        } catch (Exception e) {
            log.error("[{}] Redis 进度更新失败", taskId, e);
        }
    }

    /**
     * 全部成功完成
     * @param taskId 任务ID
     * @param successCount 成功数量
     */
    public void finish(String taskId, int successCount) {
        Map<String, String> map = new HashMap<>();
        put(map, ImportTaskField.TOTAL, successCount);
        put(map, ImportTaskField.STATUS, ImportStatus.FINISHED.name());
        put(map, ImportTaskField.PROCESSED, successCount);
        put(map, ImportTaskField.SUCCESS_COUNT, successCount);
        put(map, ImportTaskField.FAIL_COUNT, 0);
        put(map, ImportTaskField.ERROR_MSG, "");

        try {
            redis.opsForHash().putAll(ExcelRedisKeys.importTaskKey(taskId), map);
            log.info("[{}] 进度标记为 FINISHED, successCount={}", taskId, successCount);
        } catch (Exception e) {
            log.error("[{}] Redis 进度标记失败", taskId, e);
        }
    }

    /**
     * 部分成功（有失败记录）
     * @param taskId 任务ID
     * @param successCount 成功数量
     * @param failCount 失败数量
     * @param errorMsgList 错误信息列表
     */
    public void partial(String taskId, int successCount, int failCount, List<String> errorMsgList) {
        Map<String, String> map = new HashMap<>();
        put(map, ImportTaskField.TOTAL, successCount + failCount);
        put(map, ImportTaskField.STATUS, ImportStatus.PARTIAL.name());
        put(map, ImportTaskField.PROCESSED, successCount + failCount);
        put(map, ImportTaskField.SUCCESS_COUNT, successCount);
        put(map, ImportTaskField.FAIL_COUNT, failCount);
        put(map, ImportTaskField.ERROR_MSG, formatErrors(errorMsgList));

        try {
            redis.opsForHash().putAll(ExcelRedisKeys.importTaskKey(taskId), map);
            log.info("[{}] 进度标记为 PARTIAL, success={}, fail={}", taskId, successCount, failCount);
        } catch (Exception e) {
            log.error("[{}] Redis 进度标记失败", taskId, e);
        }
    }

    /**
     * 任务失败
     * @param taskId 任务ID
     * @param errorMsg 错误信息
     */
    public void fail(String taskId, String errorMsg) {
        Map<String, String> map = new HashMap<>();
        put(map, ImportTaskField.STATUS, ImportStatus.FAILED.name());
        put(map, ImportTaskField.ERROR_MSG, errorMsg != null ? errorMsg : "未知错误");

        try {
            redis.opsForHash().putAll(ExcelRedisKeys.importTaskKey(taskId), map);
            log.warn("[{}] Progress marked as FAILED, errorMsg={}", taskId, errorMsg);
        } catch (Exception e) {
            log.error("[{}] Failed to mark progress as FAILED in Redis", taskId, e);
        }
    }

    /**
     * 查询进度
     * @param taskId 任务ID
     * @return 进度 DTO
     */
    public ImportProgressDTO getProgress(String taskId) {
        try {
            Map<Object, Object> data = redis.opsForHash()
                    .entries(ExcelRedisKeys.importTaskKey(taskId));
            if (data.isEmpty()) {
                log.warn("[{}] 未找到进度记录", taskId);
                ImportProgressDTO empty = new ImportProgressDTO();
                empty.setStatus(ImportStatus.NOT_FOUND);
                return empty;
            }
            return from(data);
        } catch (Exception e) {
            log.error("[{}] Redis 进度查询失败", taskId, e);
            ImportProgressDTO empty = new ImportProgressDTO();
            empty.setStatus(ImportStatus.NOT_FOUND);
            return empty;
        }
    }

    @Override
    public void saveErrorFilePath(String taskId, String filePath) {
        String key = ExcelRedisKeys.importTaskKey(taskId);
        redis.opsForHash().put(key, ERROR_FILE_FIELD, filePath);
        // 延长 TTL 以确保错误文件可下载
        redis.expire(key, Duration.ofHours(ExcelConstants.IMPORT_TASK_TTL_HOURS));
        log.info("[{}] 错误文件路径已保存: {}", taskId, filePath);
    }

    @Override
    public String getErrorFilePath(String taskId) {
        String key = ExcelRedisKeys.importTaskKey(taskId);
        Object val = redis.opsForHash().get(key, ERROR_FILE_FIELD);
        return val == null ? null : val.toString();
    }

    /**
     * 安全 put：支持 String / Integer / List 等多种类型自动序列化
     * @param map Hash
     * @param field 键
     * @param value 值
     */
    private void put(Map<String, String> map, ImportTaskField field, Object value) {
        if (value instanceof List<?> list) {
            map.put(field.getKey(), formatErrors(list));
        } else {
            map.put(field.getKey(), String.valueOf(value));
        }
    }

    private static int parseInt(Object val) {
        if (val == null) return 0;
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String parseString(Object val) {
        return val == null ? "" : val.toString();
    }

    private static ImportStatus parseStatus(Object val) {
        if (val == null) return ImportStatus.INIT;
        try {
            return ImportStatus.valueOf(val.toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ImportStatus.INIT;
        }
    }

    /**
     * 格式化错误信息（最多保留前3条，截断防 Redis 过大）
     * @param errors 错误
     * @return 格式化之后的信息
     */
    private String formatErrors(List<?> errors) {
        if (errors == null || errors.isEmpty()) return "";
        int limit = Math.min(errors.size(), ExcelConstants.ERROR_MSG_MAX_COUNT);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            sb.append(errors.get(i));
            if (i < limit - 1) sb.append(" | ");
        }
        String result = sb.toString();
        return result.length() > ExcelConstants.ERROR_MSG_MAX_LENGTH
                ? result.substring(0, ExcelConstants.ERROR_MSG_MAX_LENGTH) + "..."
                : result;
    }

    /**
     * 从 Redis Hash 转换为 DTO
     * @param map map
     * @return DTO
     */
    private ImportProgressDTO from(Map<Object, Object> map) {
        if (map == null || map.isEmpty()) {
            ImportProgressDTO empty = new ImportProgressDTO();
            empty.setStatus(ImportStatus.NOT_FOUND);
            return empty;
        }

        ImportProgressDTO dto = new ImportProgressDTO();
        dto.setTotal(parseInt(map.get(ImportTaskField.TOTAL.getKey())));
        dto.setProcessed(parseInt(map.get(ImportTaskField.PROCESSED.getKey())));
        dto.setSuccessCount(parseInt(map.get(ImportTaskField.SUCCESS_COUNT.getKey())));
        dto.setFailCount(parseInt(map.get(ImportTaskField.FAIL_COUNT.getKey())));
        dto.setStatus(parseStatus(map.get(ImportTaskField.STATUS.getKey())));
        dto.setErrorMsg(parseString(map.get(ImportTaskField.ERROR_MSG.getKey())));
        // 检查是否有错误文件
        dto.setErrorFileExists(map.containsKey("errorFilePath") && map.get("errorFilePath") != null);
        return dto;
    }

}
