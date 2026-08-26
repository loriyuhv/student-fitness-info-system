package com.wsw.fitnesssystem.user.domain.port;

import com.wsw.fitnesssystem.user.domain.model.User;

import java.util.Optional;

/**
 * User 模块领域仓库接口（由 Infrastructure 层实现）
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 13:45
 * @since 1.0
 */
public interface UserRepository {

    /** 根据 ID 和校区查询 */
    Optional<User> findById(Long campusId, Long userId);

    /** 根据用户名（唯一索引）查询（含逻辑删除未删除的） */
    Optional<User> findByUsername(String username);

}
