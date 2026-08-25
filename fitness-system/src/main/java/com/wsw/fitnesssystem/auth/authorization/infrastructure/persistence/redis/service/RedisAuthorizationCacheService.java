package com.wsw.fitnesssystem.auth.authorization.infrastructure.persistence.redis.service;

import com.wsw.fitnesssystem.auth.authorization.application.dto.UserAuthorization;
import com.wsw.fitnesssystem.auth.authorization.application.port.AuthorizationCacheService;
import com.wsw.fitnesssystem.auth.authorization.infrastructure.persistence.redis.model.AuthRedisKeys;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
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
public class RedisAuthorizationCacheService
    implements AuthorizationCacheService {

    private static final Duration TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, UserAuthorization> userAuthRedisTemplate;

    public RedisAuthorizationCacheService(
        @Qualifier("userAuthRedisTemplate")
        RedisTemplate<String, UserAuthorization> userAuthRedisTemplate) {
        this.userAuthRedisTemplate = userAuthRedisTemplate;
    }

    @Override
    public void cache(Operator operator, UserAuthorization authorization) {
        String key = buildKey(operator);
        userAuthRedisTemplate.opsForValue().set(
            key, authorization, TTL
        );
    }

    @Override
    public UserAuthorization get(Operator operator) {

        String key = buildKey(operator);
        return userAuthRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void evict(Operator operator) {
        userAuthRedisTemplate.delete(buildKey(operator));
    }

    private String buildKey(Operator operator) {
        return AuthRedisKeys.permUserKey(operator);
    }
}
