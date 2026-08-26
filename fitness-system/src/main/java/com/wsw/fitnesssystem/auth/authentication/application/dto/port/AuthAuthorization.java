package com.wsw.fitnesssystem.auth.authentication.application.dto.port;

import com.wsw.fitnesssystem.auth.authentication.application.port.AuthorizationPort;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

/**
 * 用户授权信息（Authentication 模块端口契约）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>Authentication 模块对"授权数据"的契约定义</li>
 *   <li>作为 {@link AuthorizationPort} 接口的返回类型</li>
 *   <li><b>注意：</b>此 DTO 属于 Authentication 模块的端口契约</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 18:45
 * @since 1.0
 */
@Getter
@Builder
public class AuthAuthorization {

    /** 用户ID */
    private Long userId;

    /** 校区ID */
    private Long campusId;

    /** 角色编码集合（如 ["ADMIN", "TEACHER"]） */
    private Set<String> roles;

    /** 权限编码集合（如 ["user:view", "user:edit"]） */
    private Set<String> permissions;

}
