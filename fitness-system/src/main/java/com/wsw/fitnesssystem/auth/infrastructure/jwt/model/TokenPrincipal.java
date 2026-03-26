package com.wsw.fitnesssystem.auth.infrastructure.jwt.model;

import lombok.Builder;
import lombok.Getter;

/**
 * TokenPrincipal
 *
 * <p>JWT 生成和刷新所需的最小身份信息载体，用于 AccessToken / RefreshToken 的 Claims 构建。
 * 该类只承载技术实现所需字段，不包含业务逻辑。
 *
 * <p>设计原则：
 * 1. 放在 infrastructure 层，不依赖 Domain。
 * 2. 字段尽量最小化，避免敏感信息泄露（如 password）。
 * 3. 用于 TokenService 生成 Token。
 *
 * <p>典型用途：
 * <ul>
 *     <li>生成 AccessToken / RefreshToken 时传入</li>
 * </ul>
 * @author loriyuhv
 * @version 1.0 2026/3/22 18:00
 * @since 1.0
 */
@Getter
@Builder
public class TokenPrincipal {
    /** 用户ID，唯一标识用户身份，用于安全上下文和 Token 校验 */
    private Long userId;
    /** 校区ID，多校区场景下区分用户归属，用于权限校验或 Redis Key 构建 */
    private Long campusId;
    /** 用户名，用于生成 Token Claims，可选字段，便于前端显示或日志追踪 */
    private String username;
    /** 设备ID，用于多端登录控制和 RefreshToken 绑定，支持单设备下线 */
    private String deviceId;
    /** Token 版本号，用于全局或单用户 Token 失效控制 */
    private Integer tokenVersion;
}
