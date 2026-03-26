package com.wsw.fitnesssystem.auth.infrastructure.jwt.provider;

import com.wsw.fitnesssystem.auth.infrastructure.jwt.model.TokenPrincipal;
import com.wsw.fitnesssystem.auth.infrastructure.jwt.model.TokenType;
import com.wsw.fitnesssystem.auth.infrastructure.jwt.config.JwtConfig;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * <p>JWT令牌工具类，可以被JwtTokenService或其他组件调用</p>
 * <p>主要职责：生成和签发JWT令牌</p>
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
     * @return Access Token 字符串
     */
    public String generateAccessToken(TokenPrincipal tokenPrincipal, String jti) {
        Date now = new Date();

        return Jwts.builder()
            // ===== 标准声明（Standard Claims） =====
            .id(jti) // jti：JWT 标准唯一标识 令牌ID
            .subject("access_token") // 主题
            .issuer(jwtConfig.getIssuer()) // 签发者
            .audience().add(jwtConfig.getAudience()).and()    // 受众
            .issuedAt(now) // iat：JWT 签发时间
            .expiration(new Date(now.getTime() + jwtConfig.getExpire())) // "exp" - 过期时间

            // ===== 自定义声明 =====
            .claim("userId", tokenPrincipal.getUserId()) // 自定义声明 - 用户ID
            .claim("campusId", tokenPrincipal.getCampusId()) // 自定义声明 - 用户校区ID
            .claim("username", tokenPrincipal.getUsername()) // 自定义声明 - 用户账号
            .claim("type", TokenType.ACCESS.name()) // 自定义声明 - 令牌类型
            .claim("tokenVersion", tokenPrincipal.getTokenVersion()) // 自定义声明 -令牌版本号

            // ===== 签名 =====
            .signWith(accessTokenKey, Jwts.SIG.HS256) // 使用HS256算法签名
            .compact(); // 生成最终的JWT字符串
    }

    /**
     * 生成刷新令牌（Refresh Token）
     * @return Refresh Token 字符串
     */
    public String generateRefreshToken(TokenPrincipal tokenPrincipal, String jti) {
        Date now = new Date();

        return Jwts.builder()
            // ===== 标准声明 =====
            .id(jti)
            .subject("refresh_token")
            .issuer(jwtConfig.getIssuer())
            .audience().add(jwtConfig.getAudience()).and()
            .issuedAt(now)
            .expiration(new Date(now.getTime() + jwtConfig.getRefreshExpire()))
            // ===== 自定义声明 =====
            .claim("userId", tokenPrincipal.getUserId())
            .claim("campusId", tokenPrincipal.getCampusId())
            .claim("deviceId", tokenPrincipal.getDeviceId())
            .claim("type", TokenType.REFRESH.name())
            .claim("tokenVersion", tokenPrincipal.getTokenVersion())
            // ===== 签名 =====
            .signWith(refreshTokenKey, Jwts.SIG.HS256)
            .compact();
    }
}
