package com.wsw.fitnesssystem.auth.infrastructure.repository.redis;

import com.wsw.fitnesssystem.auth.domain.port.SessionRepository;
import com.wsw.fitnesssystem.auth.infrastructure.config.SessionProperties;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.redis.model.AuthRedisKeys;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
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
    public void saveSession(Operator operator, String accessTokenId, String refreshTokenId) {
        long now = System.currentTimeMillis();
        String onlineKey = AuthRedisKeys.onlineKey(operator);
        String refreshToAccessKey = AuthRedisKeys.refreshToAccessKey(operator);
        String accessToRefreshKey = AuthRedisKeys.accessToRefreshKey(operator);

        // 1. 保存 AccessToken：用户当前在线的 accessToken 用途：1）查看用户在线设备 2）踢掉某个token
        redisTemplate.opsForZSet().add(onlineKey, accessTokenId, now);

        // 2. 保存 refreshToken -> accessToken 用途：通过refreshToken找到旧的accessToken，使它失效，生成新token
        redisTemplate.opsForHash().put(refreshToAccessKey, refreshTokenId, accessTokenId);

        // 3. 保存 accessToken -> refreshToken 用途：通过accessToken找到旧的refreshToken
        redisTemplate.opsForHash().put(accessToRefreshKey, accessTokenId, refreshTokenId);

        // 4. 为什么都用ttl，因为这是会话声明周期，不是accessToken的生命周期
        long ttl = sessionProperties.getExpireMillis();
        redisTemplate.expire(onlineKey, ttl, TimeUnit.MILLISECONDS);
        redisTemplate.expire(refreshToAccessKey, ttl, TimeUnit.MILLISECONDS);
        redisTemplate.expire(accessToRefreshKey, ttl, TimeUnit.MILLISECONDS);

        log.info("Save session for user {} campus {}, accessTokenId {}",
                operator.userId(), operator.campusId(), accessTokenId);
    }

    @Override
    public void removeSession(Operator operator, String accessTokenId) {
        String onlineKey = AuthRedisKeys.onlineKey(operator);
        String refreshToAccessKey = AuthRedisKeys.refreshToAccessKey(operator);
        String accessToRefreshKey = AuthRedisKeys.accessToRefreshKey(operator);

        // 1. 移除在线状态
        redisTemplate.opsForZSet().remove(onlineKey, accessTokenId);

        // 2. 通过accessTokenId拿到关联的refreshTokenId
        Object refreshTokenIdObj = redisTemplate.opsForHash().get(accessToRefreshKey, accessTokenId);
        if (refreshTokenIdObj == null) {
            // token不存在，直接返回，已经失效
            return;
        }
        String refreshTokenId = refreshTokenIdObj.toString();

        // 3. 删除access侧field
        redisTemplate.opsForHash().delete(accessToRefreshKey, accessTokenId);

        // 4. 删除refresh侧field
        redisTemplate.opsForHash().delete(refreshToAccessKey, refreshTokenId);

        // 5. 加入黑名单，TTL 使用 AccessToken 过期时间
        addToBlacklist(accessTokenId, sessionProperties.getAccessTokenExpireMinutes() * 60);
    }

    @Override
    public Set<String> removeAllSessions(Operator operator) {
        // 1. 获取用户所有在线 Access Token ID 集合
        Set<String> tokenIds = getAllSessions(operator);

        if (tokenIds == null || tokenIds.isEmpty()) {
            log.info("User {} campus {} has no online session to kick.",
                    operator.userId(), operator.campusId());
            return null;
        }

        // 2. 加入黑名单
        for (String tokenId : tokenIds) {
            addToBlacklist(
                    tokenId, sessionProperties.getAccessTokenExpireMinutes() * 60);
        }

        // 3. 递增版本号，使所有旧令牌失效
        long newVersion = incrementTokenVersion(operator);

        // 4. 清除在线会话数据
        redisTemplate.delete(AuthRedisKeys.onlineKey(operator));
        redisTemplate.delete(AuthRedisKeys.refreshToAccessKey(operator));
        redisTemplate.delete(AuthRedisKeys.accessToRefreshKey(operator));

        log.info("Removed all sessions for user {} campus {}, version incremented to {}",
            operator.userId(), operator.campusId(), newVersion);

        return tokenIds;
    }

    @Override
    public Set<String> getAllSessions(Operator operator) {
        return redisTemplate.opsForZSet().range(
            AuthRedisKeys.onlineKey(operator),
            0, -1
        );
    }

    @Override
    public boolean isOnline(Operator operator, String accessTokenId) {
        Double score = redisTemplate.opsForZSet().score(
            AuthRedisKeys.onlineKey(operator), accessTokenId
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
    public Long countSessions(Operator operator) {
        return redisTemplate.opsForZSet().zCard(
            AuthRedisKeys.onlineKey(operator)
        );
    }

    @Override
    public Optional<String> getOldestSession(Operator operator) {
        Set<String> set = redisTemplate.opsForZSet().range(
            AuthRedisKeys.onlineKey(operator),
            0,
            0
        );
        if (CollectionUtils.isEmpty(set)) {
            return Optional.empty();
        }

        return set.stream().findFirst();
    }

    @Override
    public long getTokenVersion(Operator operator) {
        String key = AuthRedisKeys.tokenVersionKey(operator);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            // 首次使用，初始化为 1
            redisTemplate.opsForValue()
                    .set(key, "1", sessionProperties.getExpireMillis(), TimeUnit.MILLISECONDS);
            return 1L;
        }
        return Long.parseLong(value);
    }

    @Override
    public long incrementTokenVersion(Operator operator) {
        String key = AuthRedisKeys.tokenVersionKey(operator);
        // increment 方法会返回递增后的新值，如果 key 不存在则从 0 开始递增（返回 1）
        Long newVersion = redisTemplate.opsForValue().increment(key);
        // 可选：设置 TTL（版本号通常永久有效，也可以与用户生命周期一致）
        redisTemplate.expire(key, sessionProperties.getExpireMillis(), TimeUnit.MILLISECONDS);
        return newVersion == null ? 1 : newVersion;
    }

    @Override
    public boolean existsRefreshToken(Operator operator, String refreshTokenId) {
        String key = AuthRedisKeys.refreshToAccessKey(operator);
        return redisTemplate.opsForHash().hasKey(key, refreshTokenId);
    }

    @Override
    public void rotateRefreshToken(Operator operator, String oldRefreshTokenId, String oldAccessTokenId, String newRefreshTokenId, String newAccessTokenId) {
        long now = System.currentTimeMillis();
        long sessionTtl = sessionProperties.getExpireMillis();
        String onlineKey = AuthRedisKeys.onlineKey(operator);
        String refreshToAccessKey = AuthRedisKeys.refreshToAccessKey(operator);
        String accessToRefreshKey = AuthRedisKeys.accessToRefreshKey(operator);

        // 1. Hash：移除旧RefreshToken映射，写入新RefreshToken <-> 新AccessToken映射
        redisTemplate.opsForHash().delete(refreshToAccessKey, oldRefreshTokenId);
        redisTemplate.opsForHash().put(refreshToAccessKey, newRefreshTokenId, newAccessTokenId);

        // 2. Hash：移除旧AccessToken映射，写入新AccessToken <-> 新RefreshToken映射
        redisTemplate.opsForHash().delete(accessToRefreshKey, oldAccessTokenId);
        redisTemplate.opsForHash().put(accessToRefreshKey, newAccessTokenId, newRefreshTokenId);

        // 3. ZSet在线列表：移除旧AccessTokenId，新增新AccessTokenId
        redisTemplate.opsForZSet().remove(onlineKey, oldAccessTokenId);
        redisTemplate.opsForZSet().add(onlineKey, newAccessTokenId, now);

        // 4. 会话续期：刷新代表用户活跃，两个key统一重置TTL（和登录saveSession行为一致）
        redisTemplate.expire(refreshToAccessKey, sessionTtl, TimeUnit.MILLISECONDS);
        redisTemplate.expire(accessToRefreshKey, sessionTtl, TimeUnit.MILLISECONDS);
        redisTemplate.expire(onlineKey, sessionTtl, TimeUnit.MILLISECONDS);

        // 5. 旧AccessToken加入黑名单，拒绝后续访问
        addToBlacklist(
                oldAccessTokenId,
                sessionProperties.getAccessTokenExpireMinutes() * 60
        );
    }

    @Override
    public String getAccessTokenIdByRefreshTokenId(Operator operator, String refreshTokenId) {
        String key = AuthRedisKeys.refreshToAccessKey(operator);
        Object val = redisTemplate.opsForHash().get(key, refreshTokenId);
        if (val == null) {
            // refreshToken不存在：已登出 / 过期 / 伪造
            throw new BizException(ResultCode.REFRESH_TOKEN_INVALID);
        }
        return val.toString();
    }
}
