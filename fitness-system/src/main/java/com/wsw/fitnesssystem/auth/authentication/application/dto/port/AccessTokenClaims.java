package com.wsw.fitnesssystem.auth.authentication.application.dto.port;

import com.wsw.fitnesssystem.auth.authentication.application.port.TokenPort;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import lombok.Builder;
import lombok.Data;

/**
 * 访问令牌 Claims 端口契约（Application 层内部数据传输对象）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>作为 {@link TokenPort#parseAccessToken(String)} 方法的返回契约，
 *       定义 JWT 适配器（Infrastructure 层）解析 Access Token 后返回的数据结构</li>
 *   <li>封装构建 {@link Operator}
 *       所需的最小身份信息，用于填充 {@code SecurityContext}</li>
 *   <li><b>严禁</b>包含设备信息（deviceId）、IP 地址等 Access Token 不应承载的数据，
 *       保持令牌轻量，减少网络传输开销</li>
 * </ul>
 *
 * <p><b>与 RefreshTokenClaims 的区别（核心边界）：</b>
 * <ul>
 *   <li>{@code AccessTokenClaims} 是 <b>轻量级身份快照</b>，只包含鉴权所需的最小字段（userId, username, userType, campusId），
 *       不包含 deviceId，因为 Access Token 每次请求都携带，体积越少越好</li>
 *   <li>{@link RefreshTokenClaims} 是 <b>带设备上下文的身份凭证</b>，包含 deviceId，
 *       用于刷新令牌时的设备绑定校验和多端登录控制</li>
 * </ul>
 *
 * <p><b>典型用途：</b>
 * <ul>
 *   <li>在 {@code JwtAuthenticationFilter} 中解析 Access Token，
 *       将 Claims 转换为 {@code Operator} 并设置到 {@code SecurityContextHolder}</li>
 *   <li>在权限校验拦截器中，从 {@code Operator} 获取 userId 和 campusId 进行权限验证</li>
 *   <li>在审计日志中，记录当前操作用户的身份信息</li>
 * </ul>
 *
 * <p><b>安全设计：</b>
 * <ul>
 *   <li>{@code jti}（JWT ID）用于令牌黑名单机制，支持单点注销和强制下线</li>
 *   <li>{@code tokenVersion} 用于全局/单用户令牌失效控制，
 *       当用户修改密码或权限变更时，通过递增版本号使所有旧令牌失效</li>
 *   <li>两者结合可实现细粒度的令牌生命周期管理，无需依赖 Redis 存储大量黑名单数据</li>
 * </ul>
 *
 * <p><b>设计约束：</b>
 * <ul>
 *   <li>此 DTO 定义在 {@code dto.port} 包下，明确其作为 {@code TokenPort} 契约的定位</li>
 *   <li>不包含任何 Web/JSON 序列化注解，保持 POJO 纯净性</li>
 *   <li>字段使用包装类型（Long/Integer），允许 JWT 解析时缺失字段返回 null，
 *       避免基础类型（long/int）因 null 值抛出 NPE</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/3/22 16:56
 * @since 1.0
 */
@Data
@Builder
public class AccessTokenClaims {

    /**
     * JWT 唯一标识（JWT ID / jti）
     * <p>每个 JWT 令牌的唯一编号，用于：
     * <ul>
     *   <li>令牌黑名单：将 jti 存入 Redis 黑名单，实现单点注销</li>
     *   <li>会话追踪：关联 Redis 中的会话索引，支持踢人操作</li>
     *   <li>防重放攻击：服务端可记录已使用的 jti，拒绝重复使用</li>
     * </ul>
     */
    private String jti;

    /**
     * 校区 ID（多租户隔离）
     * <p>标识用户所属的校区/租户，用于：
     * <ul>
     *   <li>数据隔离：所有查询和操作都基于 campusId 进行过滤</li>
     *   <li>权限校验：验证用户是否有权访问该校区资源</li>
     *   <li>多校区切换：支持一个账号在多个校区间的身份切换</li>
     * </ul>
     * 注意：使用 {@code Long} 包装类型，允许解析时字段缺失返回 null
     */
    private Long campusId;

    /**
     * 用户 ID
     * <p>用户唯一标识，核心身份字段，用于：
     * <ul>
     *   <li>业务逻辑：作为所有用户相关操作的入参</li>
     *   <li>权限查询：从缓存或数据库加载用户权限</li>
     *   <li>日志审计：记录操作行为的责任人</li>
     * </ul>
     */
    private Long userId;

    /**
     * 用户账号（用户名）
     * <p>用户登录时使用的账号，用于：
     * <ul>
     *   <li>日志展示：审计日志中显示可读的用户标识</li>
     *   <li>业务验证：部分场景需要明确当前操作者是谁</li>
     * </ul>
     * 注意：不要将 username 作为业务主键，userId 才是唯一标识
     */
    private String username;

    /** 用户身份 */
    private Integer userType;

    /**
     * Token 版本号
     * <p>用于令牌失效控制策略：
     * <ul>
     *   <li><b>全局失效</b>：系统升级时更新全局版本号，使所有旧令牌失效</li>
     *   <li><b>单用户失效</b>：用户修改密码后递增该用户的 tokenVersion，
     *       使该用户的所有旧令牌强制过期</li>
     *   <li><b>权限变更</b>：修改用户角色后递增版本号，强制重新加载最新权限</li>
     * </ul>
     * 配合 Redis 存储的 tokenVersion 进行比对，不一致则拒绝访问
     */
    private Long tokenVersion;

}
