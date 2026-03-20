package com.wsw.fitnesssystem.auth.infrastructure.session;

import com.wsw.fitnesssystem.auth.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.infrastructure.jwt.service.JwtTokenService;
import com.wsw.fitnesssystem.auth.infrastructure.session.support.AuthRedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 登录会话服务
 * 负责：
 * - 生成JWT
 * - 维护Redis登录态
 * - 控制多端登录
 *
 * @author loriyuhv
 * @version 1.0 2026/1/16 12:39
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class LoginSessionServiceImpl implements LoginSessionService {
    private final JwtTokenService jwtTokenService;
    private final RedisTemplate<String, String> stringRedisTemplate;

    @Override
    public LoginSession createSession(AuthUser user) {
        // 1. 限制最多3个登录设备
        limitSession(user.getCampusId(), user.getUserId(), 3);

        // 2. 生成分布式唯一 tokenId
        String accessTokenId = UUID.randomUUID().toString();
        String refreshTokenId = UUID.randomUUID().toString();

        // 3. 生成 JWT
        String accessToken = jwtTokenService.generateAccessToken(
            user.getCampusId(),
            user.getUserId(),
            user.getUsername(),
            accessTokenId
        );

        String refreshToken = jwtTokenService.generateRefreshToken(
            user.getCampusId(),
            user.getUserId(),
            refreshTokenId
        );

        // 4. 写入 Redis（登录态）
        cacheLoginState(
            user.getCampusId(),
            user.getUserId(),
            accessTokenId,
            refreshTokenId
        );

        // 4. 返回会话对象
        return LoginSession.builder()
            .tokenId(accessTokenId)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expire(jwtTokenService.getJwtConfig().getExpire())
            .build();
    }

    /***
     * 登录成功后，把用户的 Token 关系存入 Redis
     * 主要缓存两类数据：
     * 1. 用户当前在线的 AccessToken
     * 2. RefreshToken → AccessToken 的映射关系
     * 这样可以实现：
     * - 查询用户在线状态
     * - Token刷新
     * - 单点登录 / 踢下线
     * - 强制失效Token
     * @param campusId 校区ID（多租户隔离）
     * @param userId 用户ID
     * @param accessTokenId accessToken唯一ID (jti)
     * @param refreshTokenId refreshToken唯一ID (jti)
     */
    private void cacheLoginState(
        Long campusId,
        Long userId,
        String accessTokenId,
        String refreshTokenId
    ) {
        long now = System.currentTimeMillis();

        String onlineKey = AuthRedisKeys.onlineKey(campusId, userId);
        String refreshKey = AuthRedisKeys.refreshIndexKey(campusId, userId);

        // 用户当前在线的 accessToken key: onlineKey value: 登录时间
        // 用途：1）查看用户在线设备 2）踢掉某个token
        stringRedisTemplate.opsForZSet().add(
            onlineKey,
            accessTokenId,
            now
        );

        // refreshToken -> accessToken
        // 用途：通过refreshToken找到旧的accessToken，使它失效，生成新token
        stringRedisTemplate.opsForHash().put(
            refreshKey,
            refreshTokenId,
            accessTokenId
        );

        // 为什么都用ttl，因为这是会话声明周期，不是accessToken的生命周期
        long ttl = jwtTokenService.getJwtConfig().getRefreshExpire();

        stringRedisTemplate.expire(
            onlineKey,
            ttl,
            TimeUnit.MILLISECONDS
        );

        stringRedisTemplate.expire(
            refreshKey,
            ttl,
            TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void invalidateSession(
        Long campusId,
        Long userId,
        String accessTokenId
    ) {
        String onlineKey = AuthRedisKeys.onlineKey(campusId, userId);
        stringRedisTemplate.opsForZSet().remove(onlineKey, accessTokenId);

        // 加入黑名单（否则JWT还能用）
        addToBlacklist(accessTokenId);
    }

    @Override
    public void invalidateAll(
        Long campusId,
        Long userId
    ) {
        stringRedisTemplate.delete(
            AuthRedisKeys.onlineKey(campusId, userId)
        );

        stringRedisTemplate.delete(
            AuthRedisKeys.refreshIndexKey(campusId, userId)
        );
    }

    @Override
    public boolean isOnline(
        Long campusId,
        Long userId,
        String accessTokenId
    ) {
        Double score = stringRedisTemplate.opsForZSet().score(
            AuthRedisKeys.onlineKey(campusId, userId),
            accessTokenId
        );

        return score != null;
    }

    @Override
    public void limitSession(
        Long campusId,
        Long userId,
        int maxSessions
    ) {
        String onlineKey = AuthRedisKeys.onlineKey(campusId, userId);

        // 1. 当前在线数量
        Long size = stringRedisTemplate.opsForZSet().zCard(onlineKey);

        // 2. 如果数量 < maxSessions，直接返回
        if (size == null || size < maxSessions) {
            return;
        }

        // 3. 获取最早登录的token（第一个）
        var oldestSet = stringRedisTemplate.opsForZSet().range(onlineKey, 0, 0);

        if (oldestSet == null || oldestSet.isEmpty()) {
            return;
        }

        String oldestToken = oldestSet.iterator().next();

        // 删除
        if (oldestToken != null) {
            invalidateSession(campusId, userId, oldestToken);
        }
    }

    @Override
    public void addToBlacklist(String accessTokenId) {
        // 过期时间同AccessToken
        long ttl = jwtTokenService.getJwtConfig().getExpire();

        // 黑名单 Key
        String blacklistKey = AuthRedisKeys.blacklistKey(accessTokenId);

        // 直接写入 Redis，TTL = 30分钟（accessToken生命周期）
        stringRedisTemplate.opsForValue().set(
            blacklistKey,
            "1",
            ttl, // 分钟
            TimeUnit.MINUTES
        );
    }

    @Override
    public boolean isBlacklisted(String userId, String accessTokenId) {
        return stringRedisTemplate.hasKey(AuthRedisKeys.blacklistKey(accessTokenId));
    }
}
