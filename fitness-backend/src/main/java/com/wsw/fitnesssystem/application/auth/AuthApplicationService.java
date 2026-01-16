package com.wsw.fitnesssystem.application.auth;

import com.wsw.fitnesssystem.application.auth.dto.LoginResult;
import com.wsw.fitnesssystem.common.exception.BizException;
import com.wsw.fitnesssystem.domain.auth.enums.LoginFailReason;
import com.wsw.fitnesssystem.infrastructure.audit.service.LoginAuditService;
import com.wsw.fitnesssystem.infrastructure.jwt.service.JwtTokenService;
import com.wsw.fitnesssystem.infrastructure.security.model.SecurityUser;
import com.wsw.fitnesssystem.infrastructure.security.service.LoginFailLimitService;
import com.wsw.fitnesssystem.interfaces.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户认证用例
 * 职责边界（非常重要）：
 * 1. 只负责【登录 / 登出 / 踢人】
 * 2. 只操作 Redis 登录态
 * 3. 不感知 Filter / SecurityContext
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:16
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApplicationService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LoginFailLimitService loginFailLimitService;
    private final LoginAuditService loginAuditService;

    /**
     * 登录
     * @param username 登录名（学号 / 工号）
     * @param password 明文密码
     * @param ip 登录IP地址
     * @param deviceType 登录设备类型
     * @param userAgent 登录设备
     * @return 登录结果
     */
    public LoginResult login(
        String username,
        String password,
        String ip,
        String deviceType,
        String userAgent
    ) {
        // 1. 判断账号是否被锁定
        if (loginFailLimitService.isLocked(username, ip)) {
            loginAuditService.loginFail(
                username,
                ip,
                deviceType,
                userAgent,
                LoginFailReason.ACCOUNT_LOCKED.getDesc(),
                loginFailLimitService.getMaxFailCount(username, ip),
                true
            );

            throw new BizException(ResultCode.ACCOUNT_LOCKED);
        }

        // 2. 交给 Spring Security 认证（会走 UserDetailsService）
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (BadCredentialsException e) {
            // 记录登录失败次数
            int failCount = loginFailLimitService.recordFail(username, ip);
            boolean locked = loginFailLimitService.isLocked(username, ip);

            // 登录失败审计
            loginAuditService.loginFail(
                username,
                ip,
                deviceType,
                userAgent,
                LoginFailReason.PASSWORD_ERROR.getDesc(),
                failCount,
                locked
            );

            throw new BizException(ResultCode.USER_LOGIN_ERROR);
        } catch (UsernameNotFoundException e) {
            // 记录登录失败次数
            int failCount = loginFailLimitService.recordFail(username, ip);
            boolean locked = loginFailLimitService.isLocked(username, ip);

            loginAuditService.loginFail(
                username,
                ip,
                deviceType,
                userAgent,
                LoginFailReason.USER_NOT_FOUND.getDesc(),
                failCount,
                locked
            );

            throw new BizException(ResultCode.USER_LOGIN_ERROR);
        }

        // 登录成功，清空失败次数
        loginFailLimitService.clearFail(username, ip);

        // 2. 认证成功，提取信息
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        Long userId = securityUser.getUserId();
        username = securityUser.getUsername();
        Set<String> authorities = securityUser.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

        // 生成 tokenId（分布式登录核心）
        String accessTokenId = UUID.randomUUID().toString();
        String refreshTokenId = UUID.randomUUID().toString();

        // 生成 JWT令牌
        String accessToken = jwtTokenService.generateAccessToken(userId, username, accessTokenId);
        String refreshToken = jwtTokenService.generateRefreshToken(userId, refreshTokenId);
        long expire = jwtTokenService.getJwtConfig().getExpire(); // 访问令牌过期时间
        // 写入 Redis（登录态）
        cacheLoginState(userId, accessTokenId, authorities);
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(expire / 1000);

        loginAuditService.loginSuccess(
            userId,
            username,
            accessTokenId,
            ip,
            deviceType,
            userAgent,
            expireTime
        );

        log.info("用户登录成功 userId={}, tokenId={}", userId, accessTokenId);

        // 6. 返回结果
        return LoginResult.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenId(accessTokenId)
            .expiresIn(expire)
            .build();
    }

    // ========================= 私有方法 =========================

    /**
     * 缓存登录态 & 权限
     */
    private void cacheLoginState(
        Long userId,
        String tokenId,
        Set<String> permCodes
    ) {
        // 1. 在线 token（Hash）
        String onlineKey = buildOnlineKey(userId);
        redisTemplate.opsForHash().put(
            onlineKey,
            tokenId,
            System.currentTimeMillis()
        );

        redisTemplate.expire(
            onlineKey,
            jwtTokenService.getJwtConfig().getRefreshExpire(),
            TimeUnit.MILLISECONDS
        );

        // 2. 权限缓存
        String permKey = buildPermKey(userId, tokenId);
        redisTemplate.opsForValue().set(
            permKey,
            permCodes,
            jwtTokenService.getJwtConfig().getExpire(),
            TimeUnit.MILLISECONDS
        );
    }

    private String buildOnlineKey(Long userId) {
        return "login:online:" + userId;
    }

    private String buildPermKey(Long userId, String tokenId) {
        return "login:perm:" + userId + ":" + tokenId;
    }

}
