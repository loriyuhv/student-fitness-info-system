package com.wsw.fitnesssystem.iam.authorization.infrastructure.cache;

import com.wsw.fitnesssystem.iam.authorization.application.dto.result.UserAuthorization;
import com.wsw.fitnesssystem.iam.authorization.application.port.output.AuthorizationCachePort;
import com.wsw.fitnesssystem.shared.infrastructure.properties.AuthRedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class RedisAuthorizationCacheAdapter implements AuthorizationCachePort {

    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, UserAuthorization> userAuthRedisTemplate;

    public RedisAuthorizationCacheAdapter(
        @Qualifier("userAuthRedisTemplate")
        RedisTemplate<String, UserAuthorization> userAuthRedisTemplate) {
        this.userAuthRedisTemplate = userAuthRedisTemplate;
    }

    @Override
    public void cache(long userId, long campusId, UserAuthorization authorization) {
        String key = buildKey(userId, campusId);
        userAuthRedisTemplate.opsForValue().set(
            key, authorization, TTL
        );
    }

    @Override
    public UserAuthorization get(long userId, long campusId) {
        String key = buildKey(userId, campusId);
        return userAuthRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void evict(long userId, long campusId) {
        userAuthRedisTemplate.delete(buildKey(userId, campusId));
    }

    private String buildKey(long userId, long campusId) {
        return AuthRedisKeys.permUserKey(userId, campusId);
    }

}
