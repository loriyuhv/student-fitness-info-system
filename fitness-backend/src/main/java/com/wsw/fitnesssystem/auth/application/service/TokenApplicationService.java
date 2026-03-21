package com.wsw.fitnesssystem.auth.application.service;

import com.wsw.fitnesssystem.auth.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.domain.model.TokenPair;

/**
 * Token 生成应用服务接口
 *
 * <p>负责生成用户登录所需的 Token 对（Access Token + Refresh Token）。
 * 该接口属于应用服务层，封装了 Token 生成逻辑（包括 UUID 生成、JWT 签发等），
 * 为登录流程提供统一的 Token 创建入口。
 *
 * <p>典型使用场景：
 * <ul>
 *     <li>用户登录成功后生成 Token 对</li>
 *     <li>Token 对用于会话管理、权限认证和刷新机制</li>
 * </ul>
 *
 * <p>注意：该接口仅负责生成 Token，并不负责持久化或缓存管理。
 * 持久化操作请交由 SessionRepository 或相关服务处理。
 *
 * @author loriyuhv
 * @version 1.0 2026/3/21 14:01
 * @since 1.0
 */
public interface TokenApplicationService {
    /**
     * 生成 Token 对
     *
     * <p>根据登录成功的用户信息生成唯一的 Access Token 和 Refresh Token。
     *
     * @param user 登录成功的用户信息 {@link AuthUser}
     * @return TokenPair 包含 Access Token、Refresh Token、Access Token ID、Refresh Token ID 及过期时间
     */
    TokenPair generate(AuthUser user);
}
