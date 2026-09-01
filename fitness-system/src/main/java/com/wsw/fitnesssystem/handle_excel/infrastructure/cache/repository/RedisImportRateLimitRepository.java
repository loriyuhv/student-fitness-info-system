package com.wsw.fitnesssystem.handle_excel.infrastructure.cache.repository;

import com.wsw.fitnesssystem.handle_excel.core.port.ImportRateLimitPort;
import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ExcelConstants;
import com.wsw.fitnesssystem.handle_excel.infrastructure.cache.model.ExcelRedisKeys;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 导入频率限制 — Redis 固定窗口计数器实现
 * <p>使用 Lua 脚本保证 INCR + EXPIRE 原子性</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/23 20:57
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisImportRateLimitRepository implements ImportRateLimitPort {

    private final StringRedisTemplate redis;
    private static final int RATE_LIMIT_WINDOW_SECONDS = ExcelConstants.RATE_LIMIT_WINDOW_SECONDS;
    private static final int RATE_LIMIT_MAX_COUNT = ExcelConstants.RATE_LIMIT_MAX_COUNT;
    private static final String LUA_SCRIPT = """
        local current = redis.call('incr', KEYS[1])
        
        if current == 1 then
            redis.call('expire', KEYS[1], ARGV[1])
        end
        
        return current
        """;

    @Override
    public void checkRateLimit(Long userId) {
        if (userId == null || userId <= 0) {
            return; // 未登录场景不做限制
        }

        String key = ExcelRedisKeys.limitImportKey(userId);
        Long current = redis.execute(new DefaultRedisScript<>(LUA_SCRIPT, Long.class),
                Collections.singletonList(key), String.valueOf(RATE_LIMIT_WINDOW_SECONDS));

        if (current > RATE_LIMIT_MAX_COUNT) {
            log.warn("User {} exceeded rate limit: {} requests in {} seconds",
                userId, current, RATE_LIMIT_WINDOW_SECONDS
            );
            throw new BizException(ResultCode.PARAM_INVALID,
                "Too many import requests, please try again after " + RATE_LIMIT_WINDOW_SECONDS + " seconds"
            );
        }
    }

}
