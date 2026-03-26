package com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

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

    private String key(String taskId) {
        return "import:task:" + taskId;
    }

    public void init(String taskId) {
        redis.opsForHash().put(key(taskId), "status", "INIT");
    }

    public void setTotal(String taskId, int total) {
        redis.opsForHash().put(key(taskId), "total", String.valueOf(total));
    }

    public void updateProgress(String taskId, int processed) {
        redis.opsForHash().put(key(taskId), "processed", String.valueOf(processed));
    }

    public void finish(String taskId) {
        redis.opsForHash().put(key(taskId), "status", "FINISHED");
    }

    public Map<Object, Object> getProgress(String taskId) {
        return redis.opsForHash().entries("import:task:" + taskId);
    }
}
