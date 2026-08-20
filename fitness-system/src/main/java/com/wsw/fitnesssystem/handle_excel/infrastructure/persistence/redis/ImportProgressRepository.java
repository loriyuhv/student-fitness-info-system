package com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.redis;

import com.wsw.fitnesssystem.handle_excel.interfaces.dto.ImportProgressDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:53
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class ImportProgressRepository {
    private final StringRedisTemplate redis;

    private static final Duration EXPIRE = Duration.ofHours(24);

    private String key(String taskId) {
        return "import:task:" + taskId;
    }

    /**
     * 初始化进度（解析完 Excel 后调用）
     * @param taskId 任务ID
     * @param total 总数
     */
    public void init(String taskId, int total) {
        Map<String, String> map = new HashMap<>();
        map.put("status", "PROCESSING"); // INIT -> PROCESSING -> FINISHED -> FAILED
        map.put("total", String.valueOf(total));
        map.put("processed", "0");
        map.put("successCount", "0");
        map.put("failCount", "0");
        map.put("errorMsg", "");

        String key = key(taskId);
        redis.opsForHash().putAll(key, map);
        redis.expire(key, EXPIRE);
    }

    public void setTotal(String taskId, int total) {
        redis.opsForHash().put(key(taskId), "total", String.valueOf(total));
    }


    /**
     * 更新进度（每批处理完调用）
     * @param taskId 任务ID
     * @param successCount 成功计数
     * @param failCount 失败计数
     * @param total 总数
     * @param errorMsgList 错误列表
     */
    public void updateProgress(
            String taskId, int successCount, int failCount, int total, List<String> errorMsgList) {
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
     * @param successCount 成功计数
     */
    public void finish(String taskId, int successCount) {
        Map<String, String> map = new HashMap<>();
        map.put("status", "FINISHED");
        map.put("processed", String.valueOf(successCount));
        map.put("successCount", String.valueOf(successCount));
        map.put("failCount", "0");

        redis.opsForHash().putAll(key(taskId), map);
    }

    /**
     * 部分成功（有失败记录）
     * @param taskId 任务ID
     * @param successCount 成功计数
     * @param failCount 失败计数
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
    }

    /**
     * 任务失败
     * @param taskId 任务ID
     * @param errorMsg 失败原因
     */
    public void fail(String taskId, String errorMsg) {
        Map<String, String> map = new HashMap<>();
        map.put("status", "FAILED");
        map.put("errorMsg", errorMsg != null ? errorMsg : "未知错误");

        redis.opsForHash().putAll(key(taskId), map);
    }


    /**
     * 查询进度
     * @param taskId 任务ID
     * @return 导入进度DTO
     */
    public ImportProgressDTO getProgress(String taskId) {
        Map<Object, Object> data = redis.opsForHash().entries(key(taskId));
        return ImportProgressDTO.from(data);
    }

    /**
     * 格式化错误信息（最多保留前3条，截断防Redis过大）
     * @param errors 错误
     * @return 字符串
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
