package com.wsw.fitnesssystem.auth.domain.port;

import com.wsw.fitnesssystem.auth.domain.model.User;

import java.util.Optional;

/**
 * 用户仓储端口
 * <p>domain 层只关心：</p>
 * <ul>
 *     <li>能不能拿到 User</li>
 *     <li>不关心数据来源（DB / RPC / Cache）</li>
 * </ul>
 * @author loriyuhv
 * @version 1.0 2026/1/19 14:23
 * @since 1.0
 */
public interface UserRepository {
    /**
     * 根据账号查询用户
     */
    Optional<User> findByUsername(String username);
}
