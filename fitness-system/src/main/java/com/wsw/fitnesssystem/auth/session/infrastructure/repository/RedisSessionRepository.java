package com.wsw.fitnesssystem.auth.session.infrastructure.repository;

import com.wsw.fitnesssystem.auth.session.domain.port.SessionRepository;
import com.wsw.fitnesssystem.auth.session.infrastructure.config.SessionProperties;
import com.wsw.fitnesssystem.auth.shared.infrastructure.redis.AuthRedisKeys;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
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

    // ==================== Lua 脚本定义 ====================

    private RedisScript<Long> saveSessionScript;
    private RedisScript<String> removeSessionScript;
    private RedisScript<Long> removeAllSessionsScript;
    private RedisScript<Long> rotateRefreshTokenScript;
    private RedisScript<Long> getTokenVersionScript;

    @PostConstruct
    public void init() {
        // 1. 保存会话
        String saveSessionLua = """
            local onlineKey = KEYS[1]
            local refreshToAccessKey = KEYS[2]
            local accessToRefreshKey = KEYS[3]
            local accessTokenId = ARGV[1]
            local refreshTokenId = ARGV[2]
            local now = ARGV[3]
            local ttl = ARGV[4]
            
            redis.call('ZADD', onlineKey, now, accessTokenId)
            redis.call('HSET', refreshToAccessKey, refreshTokenId, accessTokenId)
            redis.call('HSET', accessToRefreshKey, accessTokenId, refreshTokenId)
            redis.call('PEXPIRE', onlineKey, ttl)
            redis.call('PEXPIRE', refreshToAccessKey, ttl)
            redis.call('PEXPIRE', accessToRefreshKey, ttl)
            
            return 1
            """;
        saveSessionScript = new DefaultRedisScript<>(saveSessionLua, Long.class);

        // 2. 移除单个会话
        String removeSessionLua = """
            local onlineKey = KEYS[1]
            local refreshToAccessKey = KEYS[2]
            local accessToRefreshKey = KEYS[3]
            local blacklistKey = KEYS[4]
            local accessTokenId = ARGV[1]
            local expireMilliseconds = ARGV[2]
            
            redis.call('ZREM', onlineKey, accessTokenId)
            local refreshTokenId = redis.call('HGET', accessToRefreshKey, accessTokenId)
            redis.call('HDEL', accessToRefreshKey, accessTokenId)
            
            if refreshTokenId then
                redis.call('HDEL', refreshToAccessKey, refreshTokenId)
            end
            
            redis.call('SET', blacklistKey, "1", 'PX', expireMilliseconds)
            return refreshTokenId or ""
            """;

        removeSessionScript = new DefaultRedisScript<>(removeSessionLua, String.class);

        // 3. 移除所有会话（返回 token 列表，Java 端批量拉黑）
        String removeAllSessionsLua = """
            local onlineKey = KEYS[1]
            local refreshToAccessKey = KEYS[2]
            local accessToRefreshKey = KEYS[3]
            local tokenVersionKey = KEYS[4]

            local newVersion = redis.call('INCR', tokenVersionKey)

            redis.call('DEL', onlineKey)
            redis.call('DEL', refreshToAccessKey)
            redis.call('DEL', accessToRefreshKey)

            return newVersion
            """;
        removeAllSessionsScript = new DefaultRedisScript<>(removeAllSessionsLua, Long.class);

        // 4. 轮换 RefreshToken
        String rotateRefreshTokenLua = """
            local onlineKey = KEYS[1]
            local refreshToAccessKey = KEYS[2]
            local accessToRefreshKey = KEYS[3]
            local blacklistKey = KEYS[4]
            local oldRefreshTokenId = ARGV[1]
            local oldAccessTokenId = ARGV[2]
            local newRefreshTokenId = ARGV[3]
            local newAccessTokenId = ARGV[4]
            local now = ARGV[5]
            local sessionTtl = ARGV[6]
            local blacklistExpire = ARGV[7]

            redis.call('HDEL', refreshToAccessKey, oldRefreshTokenId)
            redis.call('HSET', refreshToAccessKey, newRefreshTokenId, newAccessTokenId)

            redis.call('HDEL', accessToRefreshKey, oldAccessTokenId)
            redis.call('HSET', accessToRefreshKey, newAccessTokenId, newRefreshTokenId)

            redis.call('ZREM', onlineKey, oldAccessTokenId)
            redis.call('ZADD', onlineKey, now, newAccessTokenId)

            redis.call('PEXPIRE', refreshToAccessKey, sessionTtl)
            redis.call('PEXPIRE', accessToRefreshKey, sessionTtl)
            redis.call('PEXPIRE', onlineKey, sessionTtl)

            redis.call('SET', blacklistKey, "1", 'PX', blacklistExpire)
            return 1
            """;
        rotateRefreshTokenScript = new DefaultRedisScript<>(rotateRefreshTokenLua, Long.class);

        // 5. 获取 Token 版本号（带初始化）
        String getTokenVersionLua = """
            local tokenVersionKey = KEYS[1]
            
            local currentVersion = redis.call('GET', tokenVersionKey)
            if currentVersion then
                return currentVersion
            end
            
            redis.call('SET', tokenVersionKey, 1)
            return 1
            """;
        getTokenVersionScript = new DefaultRedisScript<>(getTokenVersionLua, Long.class);
    }

    // ==================== 业务方法实现 ====================

    @Override
    public void saveSession(long campusId, long userId, String accessTokenId, String refreshTokenId) {
        long now = System.currentTimeMillis();
        long ttl = sessionProperties.getSessionExpireMillis();

        redisTemplate.execute(
            saveSessionScript,
            List.of(
                AuthRedisKeys.onlineKey(campusId, userId),
                AuthRedisKeys.refreshToAccessKey(campusId, userId),
                AuthRedisKeys.accessToRefreshKey(campusId, userId)
            ),
            accessTokenId, refreshTokenId,
            String.valueOf(now), String.valueOf(ttl)
        );

        log.info("Save session for user {} campus {}, accessTokenId {}",
                userId, campusId, accessTokenId);
    }

    @Override
    public void removeSession(long campusId, long userId, String accessTokenId) {
        String refreshTokenId = redisTemplate.execute(
            removeSessionScript,
            List.of(
                AuthRedisKeys.onlineKey(campusId, userId),
                AuthRedisKeys.refreshToAccessKey(campusId, userId),
                AuthRedisKeys.accessToRefreshKey(campusId, userId),
                AuthRedisKeys.blacklistKey(accessTokenId)
            ),
            accessTokenId,
            String.valueOf(sessionProperties.getBlacklistExpireMillis())
        );

        // refreshTokenId 为 "" 表示原本就不存在，已经失效
        if (refreshTokenId.isEmpty()) {
            log.warn("Token {} not found or already expired", accessTokenId);
        }
    }

    @Override
    public Set<String> removeAllSessions(long campusId, long userId) {
        // 1. Java查询当前在线tokenId，用于审计与接口返回
        Set<String> tokenIds = getAllSessions(campusId, userId);
        if (tokenIds == null || tokenIds.isEmpty()) {
            log.info("User {} campus {} has no online session to kick.", userId, campusId);
            return Collections.emptySet();
        }

        // 2. Lua脚本原子操作：版本自增 + 删除全部会话相关key, 返回递增后的新版本号
        Long newTokenVersion = redisTemplate.execute(
            removeAllSessionsScript,
            List.of(
                AuthRedisKeys.onlineKey(campusId, userId),
                AuthRedisKeys.refreshToAccessKey(campusId, userId),
                AuthRedisKeys.accessToRefreshKey(campusId, userId),
                AuthRedisKeys.tokenVersionKey(campusId, userId)
            )
        );

        log.info("Removed all sessions for user {} campus {}, version incremented to {}",
            userId, campusId, newTokenVersion);

        return tokenIds;
    }

    @Override
    public Set<String> getAllSessions(long campusId, long userId) {
        return redisTemplate.opsForZSet().range(
            AuthRedisKeys.onlineKey(campusId, userId),
            0, -1
        );
    }

    @Override
    public boolean isOnline(long campusId, long userId, String accessTokenId) {
        Double score = redisTemplate.opsForZSet().score(
            AuthRedisKeys.onlineKey(campusId, userId), accessTokenId
        );
        return score != null;
    }

    @Override
    public void addToBlacklist(String accessTokenId) {
        String blacklistKey = AuthRedisKeys.blacklistKey(accessTokenId);
        long ttl = sessionProperties.getBlacklistExpireMillis();
        redisTemplate.opsForValue().set(blacklistKey, "1", ttl, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isBlacklisted(String accessTokenId) {
        return redisTemplate.hasKey(AuthRedisKeys.blacklistKey(accessTokenId));
    }

    @Override
    public Long countSessions(long campusId, long userId) {
        return redisTemplate.opsForZSet().zCard(
            AuthRedisKeys.onlineKey(campusId, userId)
        );
    }

    @Override
    public Optional<String> getOldestSession(long campusId, long userId) {
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
    public long getTokenVersion(long campusId, long userId) {
        return redisTemplate.execute(
            getTokenVersionScript,
            Collections.singletonList(AuthRedisKeys.tokenVersionKey(campusId, userId))
        );
    }

    @Override
    public boolean existsRefreshToken(long campusId, long userId, String refreshTokenId) {
        String key = AuthRedisKeys.refreshToAccessKey(campusId, userId);
        return redisTemplate.opsForHash().hasKey(key, refreshTokenId);
    }

    @Override
    public void rotateRefreshToken(
        long campusId, long userId, String oldRefreshTokenId,
        String oldAccessTokenId, String newRefreshTokenId, String newAccessTokenId) {

        long now = System.currentTimeMillis();
        long sessionTtl = sessionProperties.getSessionExpireMillis();

        redisTemplate.execute(
            rotateRefreshTokenScript,
            List.of(
                AuthRedisKeys.onlineKey(campusId, userId),
                AuthRedisKeys.refreshToAccessKey(campusId, userId),
                AuthRedisKeys.accessToRefreshKey(campusId, userId),
                AuthRedisKeys.blacklistKey(oldAccessTokenId)
            ),
            oldRefreshTokenId, oldAccessTokenId,
            newRefreshTokenId, newAccessTokenId,
            String.valueOf(now),
            String.valueOf(sessionTtl),
            String.valueOf(sessionProperties.getBlacklistExpireMillis())
        );
    }

    @Override
    public String getAccessTokenIdByRefreshTokenId(long campusId, long userId, String refreshTokenId) {
        String key = AuthRedisKeys.refreshToAccessKey(campusId, userId);
        Object val = redisTemplate.opsForHash().get(key, refreshTokenId);
        if (val == null) {
            // refreshToken不存在：已登出 / 过期 / 伪造
            throw new BizException(ResultCode.REFRESH_TOKEN_INVALID);
        }
        return val.toString();
    }
}
