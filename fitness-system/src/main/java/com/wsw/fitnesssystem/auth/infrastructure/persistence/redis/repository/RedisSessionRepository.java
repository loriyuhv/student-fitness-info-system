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
    public void removeAllSessions(Long campusId, Long userId) {
        // 1. 递增版本号，使所有旧令牌失效
        long newVersion = incrementTokenVersion(campusId, userId);

        // 2. 清除在线会话数据
        redisTemplate.delete(AuthRedisKeys.onlineKey(campusId, userId));
        redisTemplate.delete(AuthRedisKeys.refreshIndexKey(campusId, userId));

        log.info("Removed all sessions for user {} campus {}, version incremented to {}",
            userId, campusId, newVersion);
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

    @Override
    public long getTokenVersion(Long campusId, Long userId) {
        String key = AuthRedisKeys.tokenVersionKey(campusId, userId);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            // 首次使用，初始化为 1
            redisTemplate.opsForValue().set(key, "1", sessionProperties.getExpireMillis(), TimeUnit.MILLISECONDS);
            return 1L;
        }
        return Long.parseLong(value);
    }

    @Override
    public long incrementTokenVersion(Long campusId, Long userId) {
        String key = AuthRedisKeys.tokenVersionKey(campusId, userId);
        // increment 方法会返回递增后的新值，如果 key 不存在则从 0 开始递增（返回 1）
        Long newVersion = redisTemplate.opsForValue().increment(key);
        // 可选：设置 TTL（版本号通常永久有效，也可以与用户生命周期一致）
        redisTemplate.expire(key, sessionProperties.getExpireMillis(), TimeUnit.MILLISECONDS);
        return newVersion == null ? 1 : newVersion;
    }
}
