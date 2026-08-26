package com.wsw.fitnesssystem.auth.authentication.application.port;

import com.wsw.fitnesssystem.auth.authentication.application.dto.port.AccessTokenClaims;
import com.wsw.fitnesssystem.auth.authentication.application.dto.port.TokenPair;
import com.wsw.fitnesssystem.auth.authentication.application.dto.port.RefreshTokenClaims;

/**
 * Token 管理端口（出站端口）
 * <p>定义应用层所需的令牌生成与解析契约，由基础设施层（如 JWT 实现）提供具体能力。</p>
 * <p>该端口屏蔽了底层令牌技术的细节（如加密算法、存储策略），使应用层仅依赖业务语义。</p>
 *
 * <p>主要职责：</p>
 * <ul>
 *   <li>在用户登录/刷新成功后，生成一对访问令牌（Access Token）和刷新令牌（Refresh Token）</li>
 *   <li>解析刷新令牌，提取其中的声明信息（如用户 ID、设备 ID、版本号等）</li>
 * </ul>
 *
 * <p>典型实现方式：基于 JWT（JJWT / Nimbus）、OAuth2 或自定义加密方案。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/3/22 16:48
 * @since 1.0
 */
public interface TokenPort {

    /**
     * 生成完整的令牌对（Access Token + Refresh Token）。
     *
     * @param deviceId 设备唯一标识（用于绑定刷新令牌到具体设备）
     * @param tokenVersion 令牌版本号（每次生成时从外部传入，通常来自用户聚合的版本字段）
     * @param accessTokenId 访问令牌 ID（用于后续缓存或撤销）
     * @param refreshTokenId 刷新令牌 ID（用于后续缓存或撤销）
     * @return 包含完整令牌信息的 {@link TokenPair}，包括令牌字符串、ID 和过期秒数
     */
    TokenPair generate(
        long campusId, long userId, String username, int userType, String deviceId,
        Long tokenVersion, String accessTokenId, String refreshTokenId
    );

    /**
     * 解析刷新令牌，提取其声明信息。
     *
     * @param accessToken 原始访问令牌字符串（非空，通常为 JWT 格式）
     * @return 解析后的 {@link AccessTokenClaims} 对象，包含业务声明
     */
    AccessTokenClaims parseAccessToken(String accessToken);

    /**
     * 解析刷新令牌，提取其声明信息。
     *
     * @param refreshToken 原始刷新令牌字符串（非空，通常为 JWT 格式）
     * @return 解析后的 {@link RefreshTokenClaims} 对象，包含业务声明
     */
    RefreshTokenClaims parseRefreshToken(String refreshToken);

}
