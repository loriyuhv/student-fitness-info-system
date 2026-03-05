package com.wsw.fitnesssystem.auth.infrastructure.jwt.utils;

import com.wsw.fitnesssystem.common.jwt.medel.TokenType;
import com.wsw.fitnesssystem.auth.infrastructure.jwt.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * JWT 解析与验证工具类
 * 主要职责：解析、验证 JWT 令牌
 * 职责边界说明：
 * - ✔ 校验签名、过期时间、Issuer、Audience、TokenType
 * - ✔ 提取 Claims（sub / jti / 自定义声明）
 * - ✘ 不做 Redis 校验
 * - ✘ 不做用户状态 / 权限校验
 * 典型调用位置：
 * - JwtAuthenticationFilter
 * - JwtTokenService
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 16:08
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenParser {
    /** JWT 配置（issuer / audience / 过期时间等）Spring Autowired自动注入 */
    private final JwtConfig jwtConfig;

    /** Access Token 专用签名密钥 */
    private final SecretKey accessTokenKey;

    /** Refresh Token 专用签名密钥 */
    private final SecretKey refreshTokenKey;

    /** 时钟偏移容忍时间（秒），用于处理服务器时间不同步 */
    private static final long CLOCK_SKEW_SECONDS = 60L;

    /* =====================================================
     * Access Token 解析
     * ===================================================== */

    /**
     * 解析并验证 Access Token
     *
     * <p>校验内容：</p>
     * <ul>
     *   <li>签名是否合法（accessTokenKey）</li>
     *   <li>是否过期（exp）</li>
     *   <li>issuer / audience 是否匹配</li>
     *   <li>TokenType 是否为 ACCESS</li>
     *   <li>jti 是否存在（用于后续撤销 / 黑名单）</li>
     * </ul>
     *
     * @param token JWT 字符串（支持 Bearer 前缀）
     * @return Claims（已验证）
     */
    public Claims parseAccessToken(String token) {
        Claims claims = parse(token, accessTokenKey, true);

        // 校验 Token 类型
        String type = claims.get("type", String.class);
        if (!TokenType.ACCESS.name().equals(type)) {
            log.warn("非法 Access Token 类型: {}", type);
            throw new BadCredentialsException("非法的 Access Token 类型");
        }

        return claims;
    }

    /* =====================================================
     * Refresh Token 解析
     * ===================================================== */

    /**
     * 解析并验证 Refresh Token
     *
     * <p>
     * Refresh Token 只用于换取新的 Access Token，
     * 不要求 audience（避免客户端限制过死）
     * </p>
     *
     * @param token Refresh Token 字符串
     * @return Claims
     */
    public Claims parseRefreshToken(String token) {
        Claims claims = parse(token, refreshTokenKey, false);

        String type = claims.get("type", String.class);
        if (!TokenType.REFRESH.name().equals(type)) {
            log.warn("非法 Refresh Token 类型: {}", type);
            throw new BadCredentialsException("非法的 Refresh Token 类型");
        }

        return claims;
    }

    /* =====================================================
     * 核心解析逻辑（私有）
     * ===================================================== */

    private Claims parse(String token, SecretKey secretKey, boolean validateAudience) {
        // 1. 基本参数校验
        if (token == null || token.trim().isEmpty()) {
            throw new BadCredentialsException("JWT 不能为空");
        }

        // 2. 移除可能的Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // 3. 创建JWT解析器并验证令牌
            JwtParserBuilder builder = Jwts.parser()
                // 设置验证密钥
                .verifyWith(secretKey)
                // 验证签发者（必须为"student-fitness"）
                .requireIssuer(jwtConfig.getIssuer())
                // 设置时钟偏移容忍时间（60秒）
                .clockSkewSeconds(CLOCK_SKEW_SECONDS);
            if (validateAudience) {
                // 验证受众（必须为"web-client"）
                builder.requireAudience(jwtConfig.getAudience());
            }

            // 构建解析器
            Jws<Claims> jws = builder
                .build()
                .parseSignedClaims(token);

            Claims claims = jws.getPayload();
            // jti 必须存在（用于撤销 / 黑名单）
            if (claims.getId() == null || claims.getId().isBlank()) {
                throw new BadCredentialsException("JWT 缺少 jti");
            }

            log.info("JWT 解析成功: sub={}, jti={}, exp={}",
                claims.getSubject(),
                claims.getId(),
                claims.getExpiration());

            // 4. 返回Claims对象
            return claims;

        } catch (ExpiredJwtException e) {
            // 6. JWT已过期
            log.warn("JWT 已过期: sub={}, exp={}",
                e.getClaims().getSubject(),
                e.getClaims().getExpiration());
            throw new CredentialsExpiredException("JWT 已过期", e);

        } catch (MalformedJwtException | UnsupportedJwtException e) {
            // 7. 不支持的JWT格式
            log.warn("JWT 格式非法: {}", e.getMessage());
            throw new BadCredentialsException("JWT 格式非法", e);

        } catch (SecurityException e) {
            // 9. 签名验证失败
            log.warn("JWT 签名验证失败: {}", e.getMessage());
            throw new BadCredentialsException("JWT 签名错误", e);

        } catch (InvalidClaimException e) {
            // 11. 声明验证失败（如issuer/audience不匹配）
            log.warn("JWT 声明校验失败: {}", e.getMessage());
            throw new BadCredentialsException("JWT 声明非法", e);

        } catch (IllegalArgumentException e) {
            // 10. 参数错误（如密钥为空等）
            log.error("JWT 参数异常", e);
            throw new AuthenticationServiceException("JWT 解析参数异常", e);

        } catch (Exception e) {
            // 12. 其他未知异常
            log.error("JWT 解析未知异常", e);
            throw new AuthenticationServiceException("JWT 服务异常", e);
        }
    }
}
