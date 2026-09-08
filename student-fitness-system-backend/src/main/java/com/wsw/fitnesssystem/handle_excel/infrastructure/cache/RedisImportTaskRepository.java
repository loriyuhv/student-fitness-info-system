package com.wsw.fitnesssystem.handle_excel.infrastructure.cache;

import com.wsw.fitnesssystem.handle_excel.domain.model.ImportTask;
import com.wsw.fitnesssystem.handle_excel.domain.repository.ImportTaskRepository;
import com.wsw.fitnesssystem.handle_excel.domain.enums.ImportStatus;
import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ImportConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 导入任务进度 Redis 实现
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
public class RedisImportTaskRepository implements ImportTaskRepository {

    private final StringRedisTemplate redis;

    // ==================== 核心仓储方法 ====================

    @Override
    public void save(ImportTask task) {
        String key = ImportRedisKeys.taskKey(task.getTaskId());
        Map<String, String> map = new HashMap<>();

        // 使用 ImportTaskField 枚举，杜绝硬编码
        put(map, ImportTaskField.STATUS, task.getStatus().name());
        put(map, ImportTaskField.TOTAL, task.getTotal());
        put(map, ImportTaskField.PROCESSED, task.getProcessed());
        put(map, ImportTaskField.SUCCESS_COUNT, task.getSuccessCount());
        put(map, ImportTaskField.FAIL_COUNT, task.getFailCount());
        put(map, ImportTaskField.ERROR_MSG, task.getErrorSummary());

        if (task.getErrorFilePath() != null) {
            put(map, ImportTaskField.ERROR_FILE_PATH, task.getErrorFilePath());
        }

        // 如果任务取消了，写入取消标记（用于 isCancelled 轮询）
        if (task.getStatus() == ImportStatus.CANCELLED) {
            map.put(ImportTaskField.CANCELLED.getKey(), "1");
        }

        try {
            redis.opsForHash().putAll(key, map);
            redis.expire(key, Duration.ofHours(ImportConfig.IMPORT_TASK_TTL_HOURS));
            log.debug("[{}] ImportTask saved, status={}, processed={}/{}",
                task.getTaskId(), task.getStatus(), task.getProcessed(), task.getTotal());
        } catch (Exception e) {
            log.error("[{}] Failed to save ImportTask", task.getTaskId(), e);
        }
    }

    @Override
    public Optional<ImportTask> findById(String taskId) {
        String key = ImportRedisKeys.taskKey(taskId);
        try {
            Map<Object, Object> entries = redis.opsForHash().entries(key);
            if (entries.isEmpty()) {
                return Optional.empty();
            }

            ImportStatus status = parseStatus(entries.get(ImportTaskField.STATUS.getKey()));
            int total = parseInt(entries.get(ImportTaskField.TOTAL.getKey()));
            int processed = parseInt(entries.get(ImportTaskField.PROCESSED.getKey()));
            int successCount = parseInt(entries.get(ImportTaskField.SUCCESS_COUNT.getKey()));
            int failCount = parseInt(entries.get(ImportTaskField.FAIL_COUNT.getKey()));
            List<String> errorSummary = parseErrorSummary(entries.get(ImportTaskField.ERROR_MSG.getKey()));
            String errorFilePath = entries.containsKey(ImportTaskField.ERROR_FILE_PATH.getKey())
                ? entries.get(ImportTaskField.ERROR_FILE_PATH.getKey()).toString()
                : null;

            // 使用包级私有构造函数重建聚合根
            ImportTask task = new ImportTask(
                taskId, status, total, processed, successCount, failCount, errorSummary, errorFilePath
            );
            return Optional.of(task);

        } catch (Exception e) {
            log.error("[{}] Failed to find ImportTask", taskId, e);
            return Optional.empty();
        }
    }

    @Override
    public void requestCancel(String taskId) {
        String key = ImportRedisKeys.taskKey(taskId);
        redis.opsForHash().put(key, ImportTaskField.CANCELLED.getKey(), "1");
        log.info("[{}] Cancellation requested", taskId);
    }

    @Override
    public boolean isCancelled(String taskId) {
        String key = ImportRedisKeys.taskKey(taskId);
        Object val = redis.opsForHash().get(key, ImportTaskField.CANCELLED.getKey());
        return "1".equals(val);
    }

    // ==================== 私有辅助方法 ====================

    private void put(Map<String, String> map, ImportTaskField field, Object value) {
        if (value instanceof List<?> list) {
            map.put(field.getKey(), formatErrors(list));
        } else {
            map.put(field.getKey(), String.valueOf(value));
        }
    }

    private ImportStatus parseStatus(Object val) {
        if (val == null) return ImportStatus.INIT;
        try {
            return ImportStatus.valueOf(val.toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ImportStatus.INIT;
        }
    }

    private int parseInt(Object val) {
        if (val == null) return 0;
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private List<String> parseErrorSummary(Object val) {
        if (val == null) return new ArrayList<>();
        String str = val.toString();
        if (str.isBlank()) return new ArrayList<>();

        // 兼容两种格式：
        // 1. JSON 数组格式：["err1","err2"] (由 formatErrors 生成)
        // 2. 管道符格式：err1 | err2 (旧版本遗留)
        if (str.startsWith("[")) {
            // 简易 JSON 解析：去掉 [ ] 和引号，按逗号分割
            return Arrays.stream(str.replaceAll("[\\[\\]\"]", "").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        } else {
            return Arrays.stream(str.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        }
    }

    private String formatErrors(List<?> errors) {
        if (errors == null || errors.isEmpty()) return "";
        int limit = Math.min(errors.size(), ImportConfig.ERROR_MSG_MAX_COUNT);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            sb.append(errors.get(i));
            if (i < limit - 1) sb.append(" | ");
        }
        String result = sb.toString();
        return result.length() > ImportConfig.ERROR_MSG_MAX_LENGTH
            ? result.substring(0, ImportConfig.ERROR_MSG_MAX_LENGTH) + "..."
            : result;
    }

}
