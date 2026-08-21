package com.wsw.fitnesssystem.handle_excel.core.progress;

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
 * 导入进度管理器
 * 基于 Redis Hash 存储任务进度，支持 24h 自动过期
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 14:17
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImportProgressManager {
    private final StringRedisTemplate redis;
    private static final Duration EXPIRE = Duration.ofHours(24);
    private static final String KEY_PREFIX = "import:task:";

    private String key(String taskId) {
        return KEY_PREFIX + taskId;
    }

    /**
     * 初始化进度（解析完 Excel 后调用）
     * @param taskId 任务ID
     * @param total 总量
     */
    public void init(String taskId, int total) {
        Map<String, String> map = new HashMap<>();
        map.put("status", "PROCESSING");
        map.put("total", String.valueOf(total));
        map.put("processed", "0");
        map.put("successCount", "0");
        map.put("failCount", "0");
        map.put("errorMsg", "");

        String k = key(taskId);
        redis.opsForHash().putAll(k, map);
        redis.expire(k, EXPIRE);
        log.debug("[{}] 进度初始化完成, total={}", taskId, total);
    }

    /**
     * 更新进度（每批处理完调用）
     * @param taskId 任务ID
     * @param successCount 成功数量
     * @param failCount 失败数量
     * @param total 总数
     * @param errorMsgList 错误数量
     */
    public void updateProgress(String taskId, int successCount, int failCount,
                               int total, List<String> errorMsgList) {
        Map<String, String> map = new HashMap<>();
        map.put("processed", String.valueOf(successCount + failCount));
        map.put("successCount", String.valueOf(successCount));
        map.put("failCount", String.valueOf(failCount));
        map.put("errorMsg", formatErrors(errorMsgList));

        redis.opsForHash().putAll(key(taskId), map);
    }

    /**
     * 全部成功完成
     * @param taskId 任务ID
     * @param successCount 成功数量
     */
    public void finish(String taskId, int successCount) {
        Map<String, String> map = new HashMap<>();
        map.put("status", "FINISHED");
        map.put("processed", String.valueOf(successCount));
        map.put("successCount", String.valueOf(successCount));
        map.put("failCount", "0");
        map.put("errorMsg", "");

        redis.opsForHash().putAll(key(taskId), map);
        log.info("[{}] 进度标记为 FINISHED, successCount={}", taskId, successCount);
    }

    /**
     * 部分成功（有失败记录）
     * @param taskId 任务ID
     * @param successCount 成功数量
     * @param failCount 失败数量
     * @param errorMsgList 错误列表
     */
    public void partial(String taskId, int successCount, int failCount, List<String> errorMsgList) {
        Map<String, String> map = new HashMap<>();
        map.put("status", "PARTIAL");
        map.put("processed", String.valueOf(successCount + failCount));
        map.put("successCount", String.valueOf(successCount));
        map.put("failCount", String.valueOf(failCount));
        map.put("errorMsg", formatErrors(errorMsgList));

        redis.opsForHash().putAll(key(taskId), map);
        log.info("[{}] 进度标记为 PARTIAL, success={}, fail={}", taskId, successCount, failCount);
    }

    /**
     * 任务失败
     * @param taskId 任务ID
     * @param errorMsg 错误数
     */
    public void fail(String taskId, String errorMsg) {
        Map<String, String> map = new HashMap<>();
        map.put("status", "FAILED");
        map.put("errorMsg", errorMsg != null ? errorMsg : "未知错误");

        redis.opsForHash().putAll(key(taskId), map);
        log.warn("[{}] 进度标记为 FAILED, errorMsg={}", taskId, errorMsg);
    }

    /**
     * 查询进度
     * @param taskId 任务ID
     * @return 进度
     */
    public ImportProgressDTO getProgress(String taskId) {
        Map<Object, Object> data = redis.opsForHash().entries(key(taskId));
        if (data == null || data.isEmpty()) {
            log.warn("[{}] 未找到进度记录", taskId);
            ImportProgressDTO empty = new ImportProgressDTO();
            empty.setStatus("NOT_FOUND");
            return empty;
        }
        return ImportProgressDTO.from(data);
    }

    /**
     * 格式化错误信息（最多保留前3条，截断防 Redis 过大）
     * @param errors 错误
     * @return 格式化之后的信息
     */
    private String formatErrors(List<String> errors) {
        if (errors == null || errors.isEmpty()) return "";
        int limit = Math.min(errors.size(), 3);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            sb.append(errors.get(i));
            if (i < limit - 1) sb.append(" | ");
        }
        String result = sb.toString();
        return result.length() > 500 ? result.substring(0, 500) + "..." : result;
    }
}
