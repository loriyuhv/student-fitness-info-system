package com.wsw.fitnesssystem.auth.domain.port;

import com.wsw.fitnesssystem.auth.domain.model.UserInfo;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/5 08:24
 * @since 1.0
 */
public interface UserInfoRepository {
    UserInfo findById(Long userId, Long campusId);
}
