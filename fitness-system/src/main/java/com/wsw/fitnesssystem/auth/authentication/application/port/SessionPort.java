package com.wsw.fitnesssystem.auth.authentication.application.port;

import java.util.Set;

/**
 * 会话管理端口（Authentication 模块定义）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>定义 Authentication 模块对会话管理能力的依赖契约</li>
 *   <li>包括保存会话、移除会话、查询会话状态等核心操作</li>
 *   <li>不依赖任何具体实现（本地/远程均透明）</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/27 07:51
 * @since 1.0
 */
public interface SessionPort {

    /**
     * 保存用户登录会话
     */
    void saveSession(long campusId, long userId, String accessTokenId, String refreshTokenId);

    /**
     * 移除单个会话
     */
    void removeSession(long campusId, long userId, String accessTokenId);

    /**
     * 移除用户所有会话（踢人）
     * @return 被移除的 AccessToken ID 集合
     */
    Set<String> removeAllSessions(long campusId, long userId);

    /**
     * 判断指定 token 是否在线
     */
    boolean isOnline(long campusId, long userId, String accessTokenId);

    /**
     * 将指定 AccessToken 加入黑名单
     */
    void addToBlacklist(String accessTokenId);

    /**
     * 判断指定 AccessToken 是否在黑名单
     */
    boolean isBlacklisted(String accessTokenId);

    /**
     * 获取 Token 版本号
     */
    long getTokenVersion(long campusId, long userId);

    /**
     * 校验 RefreshToken 是否存在
     */
    boolean existsRefreshToken(long campusId, long userId, String refreshTokenId);

    /**
     * 通过 RefreshToken 获取 AccessTokenId
     */
    String getAccessTokenIdByRefreshTokenId(long campusId, long userId, String refreshTokenId);

    /**
     * 刷新 Token 轮换
     */
    void rotateRefreshToken(long campusId, long userId, String oldRefreshTokenId,
                            String oldAccessTokenId, String newRefreshTokenId, String newAccessTokenId);

    /**
     * 限制多端登录
     */
    void limitSessions(long campusId, long userId);

}
