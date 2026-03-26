package com.wsw.fitnesssystem.auth.infrastructure.persistence.redis.repository;

import com.wsw.fitnesssystem.auth.domain.port.SessionRepository;
import com.wsw.fitnesssystem.auth.infrastructure.config.SessionProperties;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.redis.model.AuthRedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 10:25
 * @since 1.0
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisSessionRepository implements SessionRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private final SessionProperties sessionProperties;

    @Override
    public void saveSession(Long campusId, Long userId, String accessTokenId, String refreshTokenId) {
        long now = System.currentTimeMillis();
        String onlineKey = AuthRedisKeys.onlineKey(campusId, userId);
        String refreshKey = AuthRedisKeys.refreshIndexKey(campusId, userId);

        // 1. 保存 AccessToken：用户当前在线的 accessToken 用途：1）查看用户在线设备 2）踢掉某个token
        redisTemplate.opsForZSet().add(onlineKey, accessTokenId, now);

        // 2. 保存 refreshToken -> accessToken 用途：通过refreshToken找到旧的accessToken，使它失效，生成新token
        redisTemplate.opsForHash().put(refreshKey, refreshTokenId, accessTokenId);

        // 3. 为什么都用ttl，因为这是会话声明周期，不是accessToken的生命周期
        long ttl = sessionProperties.getExpireMillis();
        redisTemplate.expire(onlineKey, ttl, TimeUnit.MILLISECONDS);
        redisTemplate.expire(refreshKey, ttl, TimeUnit.MILLISECONDS);

        log.debug("Save session for user {} campus {}, accessTokenId {}", userId, campusId, accessTokenId);
    }

    @Override
    public void removeSession(Long campusId, Long userId, String accessTokenId) {
        String onlineKey = AuthRedisKeys.onlineKey(campusId, userId);
        redisTemplate.opsForZSet().remove(onlineKey, accessTokenId);

        // 加入黑名单，TTL 使用 AccessToken 过期时间
        addToBlacklist(accessTokenId, sessionProperties.getAccessTokenExpireMinutes() * 60);
    }

    @Override
    public void removeAll(Long campusId, Long userId) {
        redisTemplate.delete(AuthRedisKeys.onlineKey(campusId, userId));
        redisTemplate.delete(AuthRedisKeys.refreshIndexKey(campusId, userId));
    }

    @Override
    public Set<String> getAllSessions(Long campusId, Long userId) {
        return redisTemplate.opsForZSet().range(
            AuthRedisKeys.onlineKey(campusId, userId),
            0, -1
        );
    }

    @Override
    public boolean isOnline(Long campusId, Long userId, String accessTokenId) {
        Double score = redisTemplate.opsForZSet().score(
            AuthRedisKeys.onlineKey(campusId, userId), accessTokenId
        );
        return score != null;
    }

    @Override
    public void addToBlacklist(String accessTokenId, long expireSeconds) {
        String blacklistKey = AuthRedisKeys.blacklistKey(accessTokenId);
        redisTemplate.opsForValue().set(blacklistKey, "1", expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean isBlacklisted(String accessTokenId) {
        return redisTemplate.hasKey(AuthRedisKeys.blacklistKey(accessTokenId));
    }

    @Override
    public Long countSessions(Long campusId, Long userId) {
        return redisTemplate.opsForZSet().zCard(
            AuthRedisKeys.onlineKey(campusId, userId)
        );
    }

    @Override
    public Optional<String> getOldestSession(Long campusId, Long userId) {
        Set<String> set = redisTemplate.opsForZSet().range(
            AuthRedisKeys.onlineKey(campusId, userId),
            0,
            0
        );
        if (CollectionUtils.isEmpty(set)) {
            return Optional.empty();
        }

        return set.stream().findFirst();
    }
}
