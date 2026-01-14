package com.wsw.fitnesssystem.domain.user;

import java.util.Optional;
import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:47
 * @since 1.0
 */
public interface UserRepository {

    /**
     * 根据用户名查询用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 查询用户权限标识集合
     */
    Set<String> findPermissions(Long userId);
}
