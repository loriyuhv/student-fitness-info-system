package com.wsw.fitnesssystem.auth.domain.port;

import com.wsw.fitnesssystem.auth.domain.model.AuthUser;

import java.util.Optional;

/**
 * Repository：聚合根的持久化抽象
 * 表达的是系统需要获取 AuthUser，而不是数据库怎么查
 * 用户仓储端口
 * <p>domain 层只关心：</p>
 * <ul>
 *     <li>能不能拿到 User</li>
 *     <li>不关心数据来源（DB / RPC / Cache）</li>
 * </ul>
 * @author loriyuhv
 * @version 1.0 2026/1/16 11:31
 * @since 1.0
 */
public interface AuthUserRepository {
    Optional<AuthUser> findByUsername(String username);
}
