package com.wsw.fitnesssystem.identity.infrastructure.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/12 3:49
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class LoginFailLimitService {
    private static final int MAX_FAIL_COUNT = 5;
    private static final long FAIL_WINDOW_MINUTES = 1;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 是否被锁（任一维度命中）
     */
    public boolean isLocked(String username, String ip) {
        return getFailCount(userKey(username)) >= MAX_FAIL_COUNT
            || getFailCount(ipKey(ip)) >= MAX_FAIL_COUNT;
    }

    /**
     * 记录一次失败（双维度）
     *
     * @return 当前最大失败次数（用于审计）
     */
    public int recordFail(String username, String ip) {
        int userCount = incr(userKey(username));
        int ipCount = incr(ipKey(ip));
        return Math.max(userCount, ipCount);
    }

    /**
     * 登录成功 → 清理
     */
    public void clearFail(String username, String ip) {
        redisTemplate.delete(userKey(username));
        redisTemplate.delete(ipKey(ip));
    }

    /**
     * 获取账号+IP最大失败次数
     */
    public int getMaxFailCount(String username, String ip) {
        return Math.max(
            getFailCount(userKey(username)),
            getFailCount(ipKey(ip))
        );
    }

    // ===== private =====

    private int incr(String key) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(
                key,
                FAIL_WINDOW_MINUTES,
                TimeUnit.MINUTES
            );
        }
        return count == null ? 0 : count.intValue();
    }

    private int getFailCount(String key) {
        Object v = redisTemplate.opsForValue().get(key);
        return v == null ? 0 : Integer.parseInt(v.toString());
    }

    private String userKey(String username) {
        return "login:fail:user:" + username;
    }

    private String ipKey(String ip) {
        return "login:fail:ip:" + ip;
    }
}
