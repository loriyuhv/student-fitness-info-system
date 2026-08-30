package com.wsw.fitnesssystem.handle_excel.infrastructure.repository.redis;

import com.wsw.fitnesssystem.handle_excel.core.port.ImportFileLockPort;
import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ExcelConstants;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.redis.model.ExcelRedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * 文件导入防重锁 — Redis 实现
 *
 * @author loriyuhv
 * @version 1.0 2026/8/23 20:51
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisImportFileLockRepository implements ImportFileLockPort {

    private final StringRedisTemplate redis;

    @Override
    public boolean tryLock(String fileMd5, String taskId) {
        if (!StringUtils.hasText(fileMd5)) {
            return true;
        }

        String key = ExcelRedisKeys.lockImportKey(fileMd5);
        Boolean success = redis.opsForValue()
                .setIfAbsent(key, taskId, Duration.ofMinutes(ExcelConstants.FILE_LOCK_TTL_MINUTES));

        if (Boolean.TRUE.equals(success)) {
            log.info("File lock acquired successfully, md5={}, taskId={}", fileMd5, taskId);
        } else {
            log.warn("Failed to acquire file lock, md5={}, taskId={}", fileMd5, taskId);
        }

        return Boolean.TRUE.equals(success);
    }

    @Override
    public void releaseLock(String fileMd5) {
        if (!StringUtils.hasText(fileMd5)) {
            return;
        }

        String key = ExcelRedisKeys.lockImportKey(fileMd5);
        Boolean deleted = redis.delete(key);
        log.info("File lock released, md5={}, result={}", fileMd5, deleted);
    }

}
