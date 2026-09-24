package com.wsw.fitnesssystem.iam.authorization.infrastructure.cache;

import com.wsw.fitnesssystem.iam.authorization.application.dto.result.UserAuthorization;
import com.wsw.fitnesssystem.iam.authorization.application.port.output.AuthorizationCachePort;
import com.wsw.fitnesssystem.shared.infrastructure.json.JsonSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
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
public class RedisAuthorizationCacheAdapter implements AuthorizationCachePort {

    private static final Duration TTL = Duration.ofHours(24);

    private final JsonSerializer jsonSerializer;
    private final StringRedisTemplate stringRedisTemplate;

    // ============================================================
    // 写缓存
    // ============================================================

    @Override
    public void cache(long userId, long campusId, UserAuthorization authorization) {
        if (authorization == null) {
            return;
        }

        String key = buildKey(userId, campusId);

        // JSON 序列化：失败会抛出 JsonSerializationException，属于契约/编码错误，直接暴露
        String json = jsonSerializer.toJson(authorization);

        // Redis 写入：String 结构，纯 JSON，不带 @class
        stringRedisTemplate.opsForValue().set(key, json, TTL);
    }

    // ============================================================
    // 读缓存
    // ============================================================

    @Override
    public UserAuthorization get(long userId, long campusId) {
        String key = buildKey(userId, campusId);

        // 1. 从 Redis 读取原始 JSON 字符串
        String json = stringRedisTemplate.opsForValue().get(key);

        if (StringUtils.isEmpty(json)) {
            return null;
        }

        // 2. 反序列化为 UserAuthorization并返回
        return jsonSerializer.fromJson(json, UserAuthorization.class);
    }

    // ============================================================
    // 清除缓存
    // ============================================================

    @Override
    public void evict(long userId, long campusId) {
        String key = buildKey(userId, campusId);
        stringRedisTemplate.delete(key);
    }

    // ============================================================
    // 私有方法
    // ============================================================

    private String buildKey(long userId, long campusId) {
        return AuthorizationRedisKeys.permUserKey(campusId, userId);
    }

}
