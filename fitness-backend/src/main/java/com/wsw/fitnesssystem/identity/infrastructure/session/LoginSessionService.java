package com.wsw.fitnesssystem.identity.infrastructure.session;

import com.wsw.fitnesssystem.identity.domain.model.AuthUser;

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
    void invalidateSession(Long userId, String tokenId);

    /**
     * 注销该用户全部会话（踢人）
     */
    void invalidateAll(Long userId);

    /**
     * 判断该 token 是否仍然在线
     * - 是否被踢
     * - 是否已注销
     * @param userId 用户Id
     * @param tokenId 令牌ID
     * @return 结果
     */
    boolean isOnline(Long userId, String tokenId);
}
