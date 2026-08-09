package com.wsw.fitnesssystem.auth.infrastructure.repository.redis;

import com.wsw.fitnesssystem.auth.domain.port.LoginFailRepository;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.redis.model.AuthRedisKeys;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
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

    private String key(Operator operator) {
        Long campusId = operator.campusId();
        String username = operator.username();

        if (campusId == null) {
            return AuthRedisKeys.limitUserFailKey(username);
        }
        return AuthRedisKeys.limitUserFailKey(
            campusId + ":" + username
        );
    }

    @Override
    public int getFailCount(Operator operator) {
        String value = redisTemplate.opsForValue().get(key(operator));
        return value == null ? 0 : Integer.parseInt(value);
    }

    @Override
    public void incrementFailCount(Operator operator) {
        String key = key(operator);

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, LOCK_DURATION);
        }
    }

    @Override
    public void resetFailCount(Operator operator) {
        redisTemplate.delete(key(operator));
    }

    @Override
    public void lock(Operator operator) {
        String key = AuthRedisKeys.limitUserLockKey(operator.username());
        redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(30));
    }

    @Override
    public boolean isLocked(Operator operator) {
        return redisTemplate.hasKey(
                AuthRedisKeys.limitUserLockKey(operator.username())
        );
    }

    @Override
    public void unlock(Operator operator) {
        redisTemplate.delete(AuthRedisKeys.limitUserLockKey(operator.username()));
    }
}
