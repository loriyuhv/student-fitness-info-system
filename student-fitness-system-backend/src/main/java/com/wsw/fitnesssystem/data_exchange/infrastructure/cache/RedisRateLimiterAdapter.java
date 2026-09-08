package com.wsw.fitnesssystem.data_exchange.infrastructure.cache;

import com.wsw.fitnesssystem.data_exchange.application.port.output.RateLimiterPort;
import com.wsw.fitnesssystem.data_exchange.infrastructure.config.ImportConfig;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Redis 限流适配器（输出端口 {@link RateLimiterPort} 的实现）。
 * <p>
 * <b>限流策略：</b>固定窗口计数器（Fixed Window Counter）。
 * <p>
 * <b>实现原理：</b>
 * <ul>
 *   <li>使用 Redis {@code INCR} 命令统计时间窗口内的请求次数</li>
 *   <li>使用 Lua 脚本保证 {@code INCR + EXPIRE} 原子性</li>
 *   <li>窗口大小和上限由 {@link ImportConfig#RATE_LIMIT_WINDOW_SECONDS} 和
 *       {@link ImportConfig#RATE_LIMIT_MAX_COUNT} 控制</li>
 * </ul>
 * <p>
 * <b>Redis 数据结构：</b>
 * <pre>
 * Key:   import:limit:user:{userId}
 * Value: 窗口内已请求次数（递增整数）
 * TTL:   60 秒（窗口过期自动重置）
 * </pre>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/23 20:57
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRateLimiterAdapter implements RateLimiterPort {

    private final StringRedisTemplate redis;

    private static final int RATE_LIMIT_WINDOW_SECONDS = ImportConfig.RATE_LIMIT_WINDOW_SECONDS;
    private static final int RATE_LIMIT_MAX_COUNT = ImportConfig.RATE_LIMIT_MAX_COUNT;

    /**
     * Lua 脚本：原子执行 INCR + EXPIRE。
     * <p>为什么需要原子性？防止 INCR 后程序崩溃导致 Key 永远不过期。
     */
    private static final String LUA_SCRIPT = """
        local current = redis.call('incr', KEYS[1])
        if current == 1 then
            redis.call('expire', KEYS[1], ARGV[1])
        end
        return current
        """;

    @Override
    public void checkRateLimit(Long userId) {
        // 未登录场景不做限制
        if (userId == null || userId <= 0) {
            log.debug("Skip rate limit for anonymous user");
            return;
        }

        String key = ImportRedisKeys.rateLimitKey(userId);
        Long current = redis.execute(new DefaultRedisScript<>(LUA_SCRIPT, Long.class),
                Collections.singletonList(key), String.valueOf(RATE_LIMIT_WINDOW_SECONDS));

        if (current > RATE_LIMIT_MAX_COUNT) {
            log.warn("User {} exceeded rate limit: {} requests in {} seconds",
                userId, current, RATE_LIMIT_WINDOW_SECONDS
            );
            throw new BizException(ResultCode.PARAM_INVALID,
                "请求过于频繁，请等待 " + RATE_LIMIT_WINDOW_SECONDS + " 秒后再试"
            );
        }

        log.debug("Rate limit passed: userId={}, requests={}/{}",
            userId, current, RATE_LIMIT_MAX_COUNT);
    }

}
