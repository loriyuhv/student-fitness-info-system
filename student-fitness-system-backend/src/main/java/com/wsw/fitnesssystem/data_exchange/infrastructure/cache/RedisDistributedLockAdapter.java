package com.wsw.fitnesssystem.data_exchange.infrastructure.cache;

import com.wsw.fitnesssystem.data_exchange.application.port.output.DistributedLockPort;
import com.wsw.fitnesssystem.data_exchange.infrastructure.config.ImportConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Redis 分布式锁适配器（输出端口 {@link DistributedLockPort} 的实现）。
 * <p>
 * <b>实现原理：</b>
 * <ul>
 *   <li>使用 Redis {@code SET key value NX EX seconds} 命令实现原子性加锁</li>
 *   <li>{@code NX}（Not exists）保证同一 key 同时只有一个线程能成功</li>
 *   <li>{@code EX} 设置过期时间，作为兜底释放机制</li>
 * </ul>
 * <p>
 * <b>Redis 数据结构：</b>
 * <pre>
 * Key:   import:lock:file:{fileMd5}
 * Value: {taskId}
 * TTL:   60 分钟（由 {@link ImportConfig#FILE_LOCK_TTL_MINUTES} 控制）
 * </pre>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/23 20:51
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDistributedLockAdapter implements DistributedLockPort {

    private final StringRedisTemplate redis;

    @Override
    public boolean tryLock(String fileMd5, String taskId) {
        // 防御性检查：MD5 为空时不做锁控制
        if (!StringUtils.hasText(fileMd5)) {
            log.debug("Skip lock for empty fileMd5");
            return true;
        }

        String key = ImportRedisKeys.fileLockKey(fileMd5);
        Boolean success = redis.opsForValue()
                .setIfAbsent(key, taskId, Duration.ofMinutes(ImportConfig.FILE_LOCK_TTL_MINUTES));

        if (Boolean.TRUE.equals(success)) {
            log.debug("File lock acquired: md5={}, taskId={}", fileMd5, taskId);
        } else {
            log.warn("File lock acquisition failed: md5={}, taskId={}", fileMd5, taskId);
        }

        return Boolean.TRUE.equals(success);
    }

    @Override
    public void releaseLock(String fileMd5) {
        if (!StringUtils.hasText(fileMd5)) {
            log.debug("Skip release for empty fileMd5");
            return;
        }

        String key = ImportRedisKeys.fileLockKey(fileMd5);
        Boolean deleted = redis.delete(key);
        log.debug("File lock released: md5={}, result={}", fileMd5, deleted);
    }

}
