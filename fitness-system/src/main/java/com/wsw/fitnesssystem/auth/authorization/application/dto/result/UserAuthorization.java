package com.wsw.fitnesssystem.auth.authorization.application.dto.result;

import com.wsw.fitnesssystem.auth.authorization.application.dto.query.AuthorizationQuery;
import com.wsw.fitnesssystem.auth.authorization.application.port.AuthorizationCacheService;
import com.wsw.fitnesssystem.auth.authorization.application.service.AuthorizationQueryService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * 用户授权信息（Application 层业务输出模型）
 *  *
 *  * <p><b>职责：</b>
 *  * <ul>
 *  *   <li>承载用户授权查询的最终结果，包含角色编码和权限编码</li>
 *  *   <li>作为 {@link AuthorizationQueryService#authorize(AuthorizationQuery)}
 *  *       的返回值，供 Authentication 模块使用</li>
 *  *   <li><b>严禁</b>添加任何 Web/JSON 序列化注解（@JsonProperty、@JsonFormat 等），
 *  *       保持 POJO 的纯净性，确保其在非 Web 环境（如 Redis 序列化）中可独立复用</li>
 *  * </ul>
 *  *
 *  * <p><b>与 AuthorizationQuery 的关系：</b>
 *  * <ul>
 *  *   <li>{@link AuthorizationQuery} 是 <b>Query（查询入参）</b>，包含查询条件（userId, campusId）</li>
 *  *   <li>{@code UserAuthorization} 是 <b>Result（查询结果）</b>，包含查询到的角色和权限集合</li>
 *  *   <li>两者形成完整的读操作契约：输入 → 输出</li>
 *  * </ul>
 *  *
 *  * <p><b>典型用途：</b>
 *  * <ul>
 *  *   <li>写入 Redis 缓存：被 {@link AuthorizationCacheService}
 *  *       序列化后存入 Redis，供后续请求快速读取</li>
 *  *   <li>转换为 Spring Security 权限：被 {@code JwtAuthenticationFilter}
 *  *       转换为 {@link org.springframework.security.core.GrantedAuthority}，
 *  *       用于 {@code @PreAuthorize} 注解的权限校验</li>
 *  *   <li>审计日志：记录用户当前拥有的角色和权限快照</li>
 *  * </ul>
 *  *
 *  * <p><b>设计约束：</b>
 *  * <ul>
 *  *   <li>此 DTO 定义在 {@code dto.result} 包下，明确其作为业务输出模型的定位</li>
 *  *   <li>所有字段不可变（通过 @Data + @Builder 保证），线程安全</li>
 *  *   <li>实现 {@link Serializable}，支持 Redis 序列化存储</li>
 *  * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/16 13:47
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthorization implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long campusId;

    /** 角色编码集合 */
    private Set<String> roles;

    /** 权限编码集合 */
    private Set<String> permissions;

}