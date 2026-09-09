package com.wsw.fitnesssystem.data_exchange.infrastructure.cache;

import com.wsw.fitnesssystem.data_exchange.application.port.output.DistributedLockPort;
import com.wsw.fitnesssystem.data_exchange.infrastructure.config.ImportInfrastructureProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;

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
 * TTL:   60 分钟（由 {@link ImportInfrastructureProperties#getRedis()} 控制）
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
    private final ImportInfrastructureProperties infraProps;

    /**
     * 释放锁 Lua（compare-and-del）：仅当锁的 value（持有者 taskId）与传入 taskId 一致时才删除。
     * <p>返回 1 = 已释放；返回 0 = 锁不存在或已被其他任务重新持有（不做删除）。</p>
     */
    private static final RedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
        """
        if redis.call('get', KEYS[1]) == ARGV[1] then
            return redis.call('del', KEYS[1])
        end
        return 0
        """,
        Long.class
    );

    @Override
    public boolean tryLock(String fileMd5, String taskId) {
        // 防御性检查：MD5 为空时不做锁控制
        if (!StringUtils.hasText(fileMd5)) {
            log.debug("Skip lock for empty fileMd5");
            return true;
        }

        String key = ImportRedisKeys.fileLockKey(fileMd5);
        Boolean success = redis.opsForValue()
                .setIfAbsent(key, taskId, Duration.ofMinutes(infraProps.getRedis().getLockTtlMinutes()));

        if (Boolean.TRUE.equals(success)) {
            log.debug("File lock acquired: md5={}, taskId={}", fileMd5, taskId);
        } else {
            log.warn("File lock acquisition failed: md5={}, taskId={}", fileMd5, taskId);
        }

        return Boolean.TRUE.equals(success);
    }

    @Override
    public void releaseLock(String fileMd5, String taskId) {
        if (!StringUtils.hasText(fileMd5)) {
            log.debug("Skip release for empty fileMd5");
            return;
        }

        String key = ImportRedisKeys.fileLockKey(fileMd5);
        // 持有者校验后释放：仅当锁 value（持有者 taskId）与当前任务一致时才删除，
        // 避免 TTL 过期后旧任务释放掉新任务重新获取的锁。
        Long released = redis.execute(RELEASE_SCRIPT, List.of(key), taskId);
        log.debug("File lock released: md5={}, taskId={}, released={}", fileMd5, taskId, released);
    }

}
