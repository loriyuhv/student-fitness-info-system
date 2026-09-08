package com.wsw.fitnesssystem.user.domain.port;

import com.wsw.fitnesssystem.user.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 用户仓储接口（Domain 层 Port）
 * <p>由 Infrastructure 层实现，负责 User 聚合根的持久化</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 13:45
 * @since 1.0
 */
public interface UserRepository {

    // ==================== 查询 ====================

    /**
     * 根据校区ID + 用户ID查询
     * <p>用于认证模块获取用户认证信息</p>
     *
     * @param campusId 校区ID
     * @param userId 用户ID
     * @return 用户
     */
    Optional<User> findByCampusIdAndUserId(Long campusId, Long userId);

    /**
     * 根据用户名查询
     * <p>用于登录认证时根据用户名查找用户</p>
     *
     * @param username 用户名
     * @return 用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 批量查询已存在的用户名
     * <p>用于 Excel 导入时批量查重，避免逐条查询</p>
     *
     * @param usernames 待查重的用户名列表
     * @return 已存在的用户名集合
     */
    Set<String> findExistingUsernames(List<String> usernames);

    // ==================== 写入 ====================

    /**
     * 保存用户
     * <p>插入成功后自动回填 userId（由 MyBatis-Plus 支持）</p>
     *
     * @param user User
     */
    void save(User user);

}
