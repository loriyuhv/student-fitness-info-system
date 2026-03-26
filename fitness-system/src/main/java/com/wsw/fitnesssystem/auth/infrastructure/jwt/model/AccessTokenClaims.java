package com.wsw.fitnesssystem.auth.infrastructure.jwt.model;

import lombok.Builder;
import lombok.Data;

/**
 * AccessTokenClaims
 *
 * <p>AccessToken 解析后的 Claims 封装对象，用于安全上下文构建。
 * 只包含访问接口所需的最小字段，不包含业务逻辑。
 *
 * <p>设计原则：
 * 1. 放在 infrastructure 层。
 * 2. 字段精简，避免不必要的信息（如 deviceId）。
 * 3. 用于解析 AccessToken 后填充 SecurityContext。
 *
 * <p>典型用途：
 * <ul>
 *     <li>解析 AccessToken 后生成安全上下文信息</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/3/22 16:56
 * @since 1.0
 */
@Data
@Builder
public class AccessTokenClaims {
    /** JWT唯一标识（jti），可用于单设备下线或 Token 撤销 */
    private String jti;

    /** 用户ID，唯一标识用户身份 */
    private Long userId;

    /** 校区ID，多校区场景下区分用户归属 */
    private Long campusId;

    /** 用户账号 */
    private String username;

    /** Token 版本号，用于全局或单用户 Token 失效控制 */
    private Integer tokenVersion;
}
