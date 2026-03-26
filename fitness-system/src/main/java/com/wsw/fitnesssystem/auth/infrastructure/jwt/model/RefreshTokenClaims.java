package com.wsw.fitnesssystem.auth.infrastructure.jwt.model;

import lombok.Builder;
import lombok.Data;

/**
 * RefreshTokenClaims
 *
 * <p>RefreshToken 解析后的 Claims 封装对象，用于刷新 Token 或多端会话管理。
 * 只包含刷新所需字段，不包含业务逻辑。
 *
 * <p>设计原则：
 * 1. 放在 infrastructure 层。
 * 2. 包含 deviceId，用于多端登录控制和单设备下线。
 * 3. 用于解析 RefreshToken 后生成安全上下文或刷新逻辑。
 *
 * <p>典型用途：
 * <ul>
 *     <li>解析 RefreshToken 后生成新的 AccessToken / RefreshToken</li>
 *     <li>多端登录控制、设备绑定</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/3/22 17:01
 * @since 1.0
 */
@Data
@Builder
public class RefreshTokenClaims {

    /** JWT唯一标识（jti），用于单设备下线或 Token 撤销 */
    private String jti;

    /** 用户ID，唯一标识用户身份 */
    private Long userId;

    /** 校区ID，多校区场景下区分用户归属 */
    private Long campusId;

    /** 设备ID，用于多端登录控制和 RefreshToken 绑定 */
    private String deviceId;

    /** Token 版本号，用于全局或单用户 Token 失效控制 */
    private Integer tokenVersion;
}
