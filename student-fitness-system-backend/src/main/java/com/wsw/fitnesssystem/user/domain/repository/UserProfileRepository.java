package com.wsw.fitnesssystem.user.domain.repository;

import com.wsw.fitnesssystem.user.domain.model.UserProfile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 08:56
 * @since 1.0
 */
public interface UserProfileRepository {

    /**
     * 根据用户ID和校区ID查询用户档案
     */
    Optional<UserProfile> findByUserIdAndCampusId(Long userId, Long campusId);

    /**
     * 按用户 ID 集合批量查询（避免 N+1）。
     *
     * @param userIds 用户 ID 集合；为空返回空列表
     * @return 用户档案列表；无匹配返回空列表
     */
    List<UserProfile> findByUserIds(Collection<Long> userIds);

    /**
     * 保存用户档案
     * <p>插入场景不回填 profileId（聚合根 profileId 为 final，
     * 且业务上均通过 userId 定位，不依赖 profileId）。
     */
    void save(UserProfile profile);

}
