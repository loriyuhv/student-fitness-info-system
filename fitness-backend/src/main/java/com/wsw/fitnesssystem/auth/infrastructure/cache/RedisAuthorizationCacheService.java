package com.wsw.fitnesssystem.auth.infrastructure.cache;

import com.wsw.fitnesssystem.auth.application.authorization.dto.UserAuthorization;
import com.wsw.fitnesssystem.auth.application.port.AuthorizationCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 基于 Redis 的权限缓存实现
 *
 * @author loriyuhv
 * @version 1.0 2026/1/16 14:11
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisAuthorizationCacheService
    implements AuthorizationCacheService {

    private static final String KEY_PREFIX = "auth:user:";

    private static final Duration TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void cache(UserAuthorization authorization) {
        String key = buildKey(authorization.userId());
        redisTemplate.opsForValue().set(
            key,
            authorization,
            TTL
        );
    }

    @Override
    public UserAuthorization get(Long userId) {

        Object value =
            redisTemplate.opsForValue().get(buildKey(userId));

        if (value instanceof UserAuthorization authorization) {
            return authorization;
        }
        return null;
    }

    @Override
    public void evict(Long userId) {
        redisTemplate.delete(buildKey(userId));
    }

    private String buildKey(Long userId) {
        return KEY_PREFIX + userId;
    }
}
