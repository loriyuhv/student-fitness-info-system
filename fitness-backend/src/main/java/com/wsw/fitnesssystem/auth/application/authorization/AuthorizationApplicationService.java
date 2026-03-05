package com.wsw.fitnesssystem.auth.application.authorization;

import com.wsw.fitnesssystem.auth.application.authorization.dto.UserAuthorization;
import com.wsw.fitnesssystem.auth.domain.model.AuthUser;

/**
 * 授权服务（用例级）
 * 职责：一次性计算“用户拥有什么权限”
 *
 * @author loriyuhv
 * @version 1.0 2026/1/16 13:47
 * @since 1.0
 */
public interface AuthorizationApplicationService {
    /**
     * 对用户进行授权，返回权限快照
     */
    UserAuthorization authorize(AuthUser authUser);
}
