package com.wsw.fitnesssystem.auth.authentication.application.dto.port;

import com.wsw.fitnesssystem.auth.audit.domain.valueobject.LoginResult;
import com.wsw.fitnesssystem.auth.authentication.application.port.TokenPort;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 令牌对端口契约（Application 层内部数据传输对象）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>作为 {@link TokenPort} 接口的返回契约，定义 JWT 适配器（Infrastructure 层）与应用层之间的数据交换格式</li>
 *   <li>封装 Access Token 与 Refresh Token 的完整生成结果，包含令牌字符串、内部标识符及双过期时间</li>
 *   <li><b>严禁</b>直接返回给 Web Controller 或暴露给前端（前端只应看到 {@link LoginResult} 的子集）</li>
 * </ul>
 *
 * <p><b>与 LoginResult 的本质区别（核心边界）：</b>
 * <ul>
 *   <li>{@code TokenPair} 是 <b>端口契约（Port Contract）</b>，面向基础设施适配器（JwtTokenAdapter），
 *       包含后端存储所需的内部 ID（accessTokenId / refreshTokenId）</li>
 *   <li>{@code LoginResult} 是 <b>业务结果（Business Result）</b>，面向 Web 层和前端，
 *       只包含客户端需要的 JWT 字符串和访问令牌过期时间</li>
 * </ul>
 *
 * <p><b>为什么需要内部 ID（accessTokenId / refreshTokenId）：</b>
 * <ul>
 *   <li>JWT 字符串虽然包含 jti（JWT ID），但每次校验都需要解析和验签，性能开销较大</li>
 *   <li>将 UUID 作为独立 ID 存储在 Redis 中，可实现 O(1) 复杂度的令牌吊销、黑名单校验和会话查询</li>
 *   <li>踢人、强制下线等操作只需操作 Redis 中的 ID 索引，无需解析 JWT 内容</li>
 * </ul>
 *
 * <p><b>典型用途：</b>
 * <ul>
 *   <li>登录成功后，由 TokenPort 生成并返回给 AuthApplicationService</li>
 *   <li>刷新令牌时，生成新的 TokenPair 并完成旧令牌的轮换（Refresh Token Rotation）</li>
 *   <li>应用层将其拆解，分别存储至 Redis（会话持久化）和返回给前端（通过 LoginResult 转换）</li>
 * </ul>
 *
 * <p><b>设计约束：</b>
 * <ul>
 *   <li>此 DTO 定义在 {@code dto.port} 包下，明确其“基础设施通信专用”的定位</li>
 *   <li>不包含任何 Web/JSON 序列化注解（@JsonProperty、@JsonFormat 等），保持纯净</li>
 *   <li>所有字段为不可变（final 语义通过 @Builder + @Getter 保证），确保线程安全</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/3/21 10:09
 * @since 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenPair {

    /**
     * 访问令牌唯一标识（UUID）
     * <p>用于 Redis 存储会话索引、黑名单吊销、查询用户在线状态等场景
     * <p>注意：此 ID 与 JWT 载荷中的 jti 字段对应，但作为独立字段存在，
     * 便于在不解析 JWT 的情况下进行会话管理
     */
    private String accessTokenId;

    /**
     * 刷新令牌唯一标识（UUID）
     * <p>用于刷新令牌轮换（Refresh Token Rotation）时的旧令牌查找与删除
     * <p>在踢人场景中，可通过此 ID 快速定位并清除对应的 RefreshToken 会话
     */
    private String refreshTokenId;

    /**
     * 访问令牌字符串（JWT）
     * <p>客户端需在后续请求的 Authorization 头中携带此令牌
     * <p>格式：Bearer {accessToken}
     */
    private String accessToken;

    /**
     * 刷新令牌字符串（JWT）
     * <p>客户端在 AccessToken 过期后，通过此令牌调用刷新接口获取新的令牌对
     * <p>应保存在客户端的安全存储中（如移动端 Keychain，Web 端 HttpOnly Cookie）
     */
    private String refreshToken;

    /**
     * 访问令牌有效期（单位：秒）
     * <p>典型值：7200（2 小时），可根据安全策略配置
     * <p>用于返回给前端进行本地倒计时或过期前主动刷新
     */
    private long accessTokenExpiresIn;

    /**
     * 刷新令牌有效期（单位：秒）
     * <p>典型值：604800（7 天），必须长于 AccessToken 的有效期
     * <p>此字段用于后端存储和过期校验，通常不返回给前端
     */
    private long refreshTokenExpiresIn;

}
