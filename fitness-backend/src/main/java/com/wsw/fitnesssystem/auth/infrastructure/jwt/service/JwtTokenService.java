package com.wsw.fitnesssystem.auth.infrastructure.jwt.service;

import com.wsw.fitnesssystem.auth.infrastructure.jwt.config.JwtConfig;
import com.wsw.fitnesssystem.auth.infrastructure.jwt.utils.JwtTokenParser;
import com.wsw.fitnesssystem.auth.infrastructure.jwt.utils.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/***
 * JWT Token 业务服务
 *
 * <p>
 * 核心职责：
 * <ul>
 *   <li>登录成功后签发 Access / Refresh Token</li>
 *   <li>刷新 Access Token</li>
 *   <li>撤销 Token（退出登录 / 被踢下线）</li>
 *   <li>和 Redis 协作管理 Token 生命周期</li>
 * </ul>
 *
 * <p>
 * 不负责：
 * <ul>
 *   <li>JWT 解析（交给 JwtTokenParser）</li>
 *   <li>JWT 生成细节（交给 JwtTokenProvider）</li>
 *   <li>用户认证（账号密码校验在 AuthService）</li>
 *   <li>权限加载（Security Filter / Authorization 再做）</li>
 * </ul>
 *
 * <p>
 * 协作组件：
 * <ul>
 *   <li>{@link JwtTokenProvider}：只负责生成 JWT</li>
 *   <li>{@link JwtTokenParser}：只负责解析 / 校验 JWT</li>
 *   <li>Redis：管理 Token 状态</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/15 1:30
 * @since 1.0
 */
@Slf4j
@Service
@Getter
@RequiredArgsConstructor
public class JwtTokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenParser jwtTokenParser;
    private final JwtConfig jwtConfig;

    /***
     * 生成JWT访问令牌 AccessToken
     * @param userId 用户唯一标识（Long类型）
     * @param username 用户名/登录名（String类型）
     * @param accessTokenId 访问令牌唯一标识，可用于黑名单/撤销（String类型）
     * @return 生成的JWT访问令牌字符串
     */
    public String generateAccessToken(Long userId, String username, String accessTokenId) {
        return jwtTokenProvider.generateAccessToken(userId, username, accessTokenId);
    }

    /***
     * 生成JWT刷新令牌 RefreshToken
     * @param userId 用户唯一标识（Long类型）
     * @param refreshTokenId 刷新令牌唯一标识
     * @return 生成的JWT刷新令牌字符串
     */
    public String generateRefreshToken(Long userId, String refreshTokenId) {
        return jwtTokenProvider.generateRefreshToken(userId, refreshTokenId);
    }

    /***
     * 解析JWT访问令牌
     * @param accessToken JWT访问令牌
     * @return Claims对象
     */
    public Claims parseAccessToken(String accessToken) {
        return jwtTokenParser.parseAccessToken(accessToken);
    }

    /***
     * 解析JWT刷新令牌
     * @param refreshToken JWT刷新令牌
     * @return Claims对象
     */
    public Claims parseRefreshToken(String refreshToken) {
        return jwtTokenParser.parseRefreshToken(refreshToken);
    }
}
