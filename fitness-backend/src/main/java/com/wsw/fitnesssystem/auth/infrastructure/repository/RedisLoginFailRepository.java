package com.wsw.fitnesssystem.auth.infrastructure.repository;

import com.wsw.fitnesssystem.auth.domain.port.LoginFailRepository;
import com.wsw.fitnesssystem.auth.infrastructure.session.support.AuthRedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 12:17
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class RedisLoginFailRepository implements LoginFailRepository {
    private final StringRedisTemplate redisTemplate;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(30);

    private String key(Long campusId, String username) {
        if (campusId == null) {
            return AuthRedisKeys.limitUserFailKey(username);
        }
        return AuthRedisKeys.limitUserFailKey(
            campusId + ":" + username
        );
    }

    @Override
    public int getFailCount(Long campusId, String username) {
        String value = redisTemplate.opsForValue().get(key(campusId, username));
        return value == null ? 0 : Integer.parseInt(value);
    }

    @Override
    public void incrementFailCount(Long campusId, String username) {
        String key = key(campusId, username);

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, LOCK_DURATION);
        }
    }

    @Override
    public void resetFailCount(Long campusId, String username) {
        redisTemplate.delete(key(campusId, username));
    }

    @Override
    public void lock(Long campusId, String username) {
        String key = AuthRedisKeys.limitUserLockKey(username);
        redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(30));
    }

    @Override
    public boolean isLocked(Long campusId, String username) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(
            AuthRedisKeys.limitUserLockKey(username)
        ));
    }

    @Override
    public void unlock(Long campusId, String username) {
        redisTemplate.delete(AuthRedisKeys.limitUserLockKey(username));
    }
}
