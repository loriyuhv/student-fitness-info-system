package com.wsw.fitnesssystem.identity.infrastructure.session;

import com.wsw.fitnesssystem.identity.domain.model.AuthUser;
import com.wsw.fitnesssystem.identity.infrastructure.jwt.service.JwtTokenService;
import com.wsw.fitnesssystem.identity.infrastructure.session.support.LoginRedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 一次登录会话的聚合结果
 *
 * @author loriyuhv
 * @version 1.0 2026/1/16 12:39
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class LoginSessionServiceImpl implements LoginSessionService {
    private final JwtTokenService jwtTokenService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public LoginSession createSession(AuthUser user) {
        // 1. 生成分布式唯一 tokenId
        String accessTokenId = UUID.randomUUID().toString();
        String refreshTokenId = UUID.randomUUID().toString();

        // 2. 生成 JWT
        String accessToken = jwtTokenService.generateAccessToken(
            user.getUserId(),
            user.getUsername(),
            accessTokenId
        );

        String refreshToken = jwtTokenService.generateRefreshToken(
            user.getUserId(),
            refreshTokenId
        );

        // 3. 写入 Redis（登录态）
        cacheLoginState(
            user.getUserId(),
            accessTokenId
        );

        // 4. 返回会话对象
        return LoginSession.builder()
            .tokenId(accessTokenId)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expire(jwtTokenService.getJwtConfig().getExpire())
            .build();
    }

    /**
     * 缓存登录态 & 权限
     */
    private void cacheLoginState(
        Long userId,
        String tokenId
    ) {
        // onlineKey：记录用户当前所有在线 token
        String onlineKey = "login:online:" + userId;

        redisTemplate.opsForHash().put(
            onlineKey,
            tokenId,
            System.currentTimeMillis()
        );

        // onlineKey 生命周期 = refreshToken 生命周期
        redisTemplate.expire(
            onlineKey,
            jwtTokenService.getJwtConfig().getRefreshExpire(),
            TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void invalidateSession(Long userId, String tokenId) {
        redisTemplate.opsForHash()
            .delete("login:online:" + userId, tokenId);
        redisTemplate
            .delete("login:perm:" + userId + ":" + tokenId);
    }

    @Override
    public void invalidateAll(Long userId) {
        redisTemplate.delete("login:online:" + userId);
    }

    @Override
    public boolean isOnline(Long userId, String tokenId) {
        Boolean exists = redisTemplate.opsForHash()
            .hasKey(LoginRedisKeys.onlineKey(userId), tokenId);
        return Boolean.TRUE.equals(exists);
    }
}
