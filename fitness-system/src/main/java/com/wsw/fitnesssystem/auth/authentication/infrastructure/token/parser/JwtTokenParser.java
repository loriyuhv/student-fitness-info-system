package com.wsw.fitnesssystem.auth.authentication.infrastructure.token.parser;

import com.wsw.fitnesssystem.auth.authentication.application.dto.AccessTokenClaims;
import com.wsw.fitnesssystem.auth.authentication.application.dto.RefreshTokenClaims;
import com.wsw.fitnesssystem.auth.authentication.infrastructure.token.model.TokenType;
import com.wsw.fitnesssystem.auth.authentication.infrastructure.config.JwtConfig;
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
    public AccessTokenClaims parseAccessToken(String token) {
        Claims claims;

        try {
            claims = parse(token, accessTokenKey, true);

            // 校验 Token 类型
            String type = claims.get("type", String.class);
            if (!TokenType.ACCESS.name().equals(type)) {
                log.warn("非法 Access Token 类型: {}", type);
                throw new BadCredentialsException("非法的 Access Token 类型");
            }

            return AccessTokenClaims.builder()
                    .jti(claims.getId())
                    .userId(claims.get("userId", Long.class))
                    .campusId(claims.get("campusId", Long.class))
                    .username(claims.get("username", String.class))
                    .tokenVersion(claims.get("tokenVersion", Long.class))
                    .build();
        } catch (IncorrectClaimException e) {
            log.warn("AccessToken 必要声明缺失或类型错误", e);
            throw new BadCredentialsException("无效访问凭证", e);
        }
    }

    /**
     * 解析并校验刷新令牌 RefreshToken
     * <p>流程：
     * <ul>
     *     <li>调用底层通用解析方法完成签名、签发者、时钟偏移校验</li>
     *     <li>强制校验token类型必须为 REFRESH，禁止使用AccessToken充当刷新令牌</li>
     *     <li>提取自定义载荷，组装 {@link RefreshTokenClaims} 返回</li>
     * </ul>
     *
     * <p>异常说明：
     * <ul>
     *     <li>{@link CredentialsExpiredException}：刷新令牌已过期</li>
     *     <li>{@link BadCredentialsException}：令牌为空、格式非法、签名错误、声明校验失败、token类型不匹配、必要载荷缺失或类型错误</li>
     *     <li>{@link AuthenticationServiceException}：密钥配置异常、解析器内部服务故障</li>
     * </ul>
     *
     * @param token 原始刷新令牌（可携带Bearer前缀）
     * @return 刷新令牌载荷模型
     * @throws BadCredentialsException         令牌无效、类型不符、载荷字段非法
     * @throws CredentialsExpiredException     刷新令牌过期
     * @throws AuthenticationServiceException  令牌解析服务内部异常
     */
    public RefreshTokenClaims parseRefreshToken(String token) {
        Claims claims;

        try {
            claims = parse(token, refreshTokenKey, false);

            String type = claims.get("type", String.class);
            if (!TokenType.REFRESH.name().equals(type)) {
                log.warn("非法RefreshToken类型: {}", type);
                throw new BadCredentialsException("非法的RefreshToken类型");
            }

            return RefreshTokenClaims.builder()
                    .jti(claims.getId())
                    .userId(claims.get("userId", Long.class))
                    .campusId(claims.get("campusId", Long.class))
                    .deviceId(claims.get("deviceId", String.class))
                    .tokenVersion(claims.get("tokenVersion", Long.class))
                    .build();
        } catch (IncorrectClaimException e) {
            log.warn("RefreshToken 必要声明缺失或类型错误", e);
            throw new BadCredentialsException("无效刷新凭证", e);
        }
    }

    /**
     * 解析并校验JWT令牌
     * <p>校验规则：
     * <ul>
     *     <li>自动剔除Bearer前缀，空令牌直接拒绝</li>
     *     <li>使用指定密钥校验签名合法性</li>
     *     <li>强制校验签发者iss，可选开启受众aud校验</li>
     *     <li>支持时钟偏移容错，避免客户端服务端时间微小差异导致校验失败</li>
     *     <li>强制校验jti不能为空，用于令牌黑名单/下线管控</li>
     * </ul>
     *
     * <p>异常转换规则：将JJWT底层异常统一转为SpringSecurity标准认证异常，上层无需依赖io.jsonwebtoken包
     * <ul>
     *     <li>{@link ExpiredJwtException} → {@link CredentialsExpiredException} 凭证过期</li>
     *     <li>格式错误、签名失败、声明不匹配 → {@link BadCredentialsException} 无效凭证</li>
     *     <li>密钥配置错误、解析器构造参数异常、未知异常 → {@link AuthenticationServiceException} 服务内部故障</li>
     * </ul>
     * @param token 原始令牌（允许携带Bearer前缀）
     * @param secretKey 签名校验密钥
     * @param validateAudience 是否校验受众aud
     * @return 解析成功返回载荷Claims
     */
    private Claims parse(String token, SecretKey secretKey, boolean validateAudience) {
        // 1. 基本参数校验
        if (token == null || token.trim().isEmpty()) {
            throw new BadCredentialsException("访问凭证不能为空");
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
            Jws<Claims> jws = builder.build().parseSignedClaims(token);
            Claims claims = jws.getPayload();

            // jti 必须存在（用于撤销 / 黑名单）
            if (claims.getId() == null || claims.getId().isBlank()) {
                throw new BadCredentialsException("访问凭证缺少唯一标识jti");
            }

            log.debug("JWT 解析成功: sub={}, jti={}, exp={}",
                claims.getSubject(),
                claims.getId(),
                claims.getExpiration());

            // 4. 返回Claims对象
            return claims;
        } catch (ExpiredJwtException e) {
            // JWT已过期
            log.warn("JWT 已过期: sub={}, exp={}",
                e.getClaims().getSubject(),
                e.getClaims().getExpiration());
            throw new CredentialsExpiredException("访问凭证已过期", e);
        } catch (MalformedJwtException | UnsupportedJwtException e) {
            // 不支持的JWT格式
            log.warn("JWT 格式非法: {}", e.getMessage());
            throw new BadCredentialsException("无效访问凭证", e);
        } catch (SecurityException e) {
            // 签名验证失败
            log.warn("JWT 签名验证失败: {}", e.getMessage());
            throw new BadCredentialsException("无效访问凭证", e);
        } catch (InvalidClaimException e) {
            // 声明验证失败（如issuer/audience不匹配）
            log.warn("JWT 声明校验失败: {}", e.getMessage());
            throw new BadCredentialsException("无效访问凭证", e);
        } catch (JwtException e) {
            // 其余所有JJWT相关异常兜底
            log.warn("JWT解析异常:{}", e.getMessage());
            throw new BadCredentialsException("无效访问凭证", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT解析构造参数异常", e);
            throw new AuthenticationServiceException("令牌校验服务内部错误", e);
        } catch (Exception e) {
            log.error("JWT解析未知异常", e);
            throw new AuthenticationServiceException("令牌校验服务内部错误", e);
        }
    }
}
