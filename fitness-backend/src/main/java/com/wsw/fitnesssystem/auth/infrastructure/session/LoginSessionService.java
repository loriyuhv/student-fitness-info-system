package com.wsw.fitnesssystem.auth.infrastructure.session;

import com.wsw.fitnesssystem.auth.domain.model.AuthUser;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/16 12:38
 * @since 1.0
 */
public interface LoginSessionService {
    /**
     * 创建登录会话
     * - 生成 tokenId
     * - 生成 JWT
     * - 写 Redis
     */
    LoginSession createSession(AuthUser user);

    /**
     * 注销单个会话
     */
    void invalidateSession(
        Long campusId,
        Long userId,
        String accessTokenId
    );

    /**
     * 注销该用户全部会话（踢人）
     */
    void invalidateAll(
        Long campusId,
        Long userId
    );

    /**
     * 判断该 token 是否仍然在线
     * - 是否被踢
     * - 是否已注销
     * @param userId 用户Id
     * @param accessTokenId 令牌ID
     * @return 结果
     */
    boolean isOnline(
        Long campusId,
        Long userId,
        String accessTokenId
    );

    /**
     * 限制会话数量
     * @param userId 用户Id
     * @param maxSessions 最大会话数
     */
    void limitSession(
        Long campusId,
        Long userId,
        int maxSessions
    );

    /***
     * Token添加Redis黑名单列表
     * @param accessTokenId AccessTokenID
     */
    void addToBlacklist(String accessTokenId);

    /***
     * 判断该AccessToken是否在黑名单列表
     * @param userId 用户ID
     * @param accessTokenId AccessTokenID
     * @return 是否
     */
    boolean isBlacklisted(String userId, String accessTokenId);
}
