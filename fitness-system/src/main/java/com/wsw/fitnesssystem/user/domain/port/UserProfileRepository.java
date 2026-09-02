package com.wsw.fitnesssystem.user.domain.port;

import com.wsw.fitnesssystem.user.domain.model.UserProfile;

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
     * 保存用户档案
     */
    void save(UserProfile profile);

}
