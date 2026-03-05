package com.wsw.fitnesssystem.auth.infrastructure.jwt.utils;

import com.wsw.fitnesssystem.common.jwt.medel.TokenType;
import com.wsw.fitnesssystem.auth.infrastructure.jwt.config.JwtConfig;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT令牌工具类，可以被JwtTokenService或其他组件调用
 * 主要职责：生成、签发 JWT 令牌
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 16:08
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtConfig jwtConfig;
    /** Access Token 专用签名密钥 */
    private final SecretKey accessTokenKey;

    /** Refresh Token 专用签名密钥 */
    private final SecretKey refreshTokenKey;

    /***
     * 生成访问令牌（Access Token）
     * @param userId 用户唯一标识（Long类型）
     * @param username 用户登录名（String类型）
     * @param accessTokenId 令牌唯一标识（jti），用于黑名单 / 撤销控制（String类型）
     * @return Access Token 字符串
     */
    public String generateAccessToken(Long userId, String username, String accessTokenId) {
        Date now = new Date();

        return Jwts.builder()
            // ===== 标准声明（Standard Claims） =====
            .issuer(jwtConfig.getIssuer()) // 签发者
            .subject(String.valueOf(userId)) // 主题（用户ID）
            .audience().add(jwtConfig.getAudience()).and()    // 受众
            .id(accessTokenId) // jti：JWT 标准唯一标识 令牌ID
            .issuedAt(new Date()) // iat：JWT 签发时间
            .expiration(new Date(now.getTime() + jwtConfig.getExpire())) // "exp" - 过期时间
            // ===== 自定义声明 =====
            .claim("username", username) // 自定义声明 - 用户名称
            .claim("type", TokenType.ACCESS.name()) // 自定义声明 - 令牌类型

            // ===== 签名 =====
            .signWith(accessTokenKey, Jwts.SIG.HS256) // 使用HS256算法签名
            .compact(); // 生成最终的JWT字符串
    }

    /**
     * 生成刷新令牌（Refresh Token）
     *
     * @param userId 用户唯一标识
     * @param refreshTokenId 刷新令牌唯一标识（用于 Redis 校验 / 撤销）
     * @return Refresh Token 字符串
     */
    public String generateRefreshToken(Long userId, String refreshTokenId) {
        Date now = new Date();

        return Jwts.builder()
            // ===== 标准声明 =====
            .issuer(jwtConfig.getIssuer())
            .subject(String.valueOf(userId))
            .id(refreshTokenId)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + jwtConfig.getRefreshExpire()))

            // ===== 自定义声明 =====
            .claim("type", TokenType.REFRESH.name())

            // ===== 签名 =====
            .signWith(refreshTokenKey, Jwts.SIG.HS256)
            .compact();
    }
}
