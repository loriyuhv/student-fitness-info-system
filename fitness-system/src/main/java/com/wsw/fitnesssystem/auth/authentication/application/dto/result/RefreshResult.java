package com.wsw.fitnesssystem.auth.authentication.application.dto.result;

import lombok.Builder;
import lombok.Data;

/**
 * 刷新令牌业务输出模型（Application 层纯数据对象）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>承载刷新令牌接口（refresh token）的业务处理结果</li>
 *   <li>作为 Application Service 的返回值，供 Controller 调用方使用</li>
 *   <li><b>严禁</b>添加任何 Web/JSON 序列化注解（如 @JsonProperty、@JsonFormat），
 *       保持 POJO 的纯净性</li>
 * </ul>
 *
 * <p><b>与 LoginResult 的区别：</b>
 * <ul>
 *   <li>{@link LoginResult} 用于首次登录场景，包含完整的用户身份信息初始化</li>
 *   <li>{@code RefreshResult} 专用于令牌续期场景，只返回新的令牌对</li>
 *   <li>虽然字段结构相同，但语义场景不同，独立的类有利于后续扩展差异化字段
 *       （如刷新时可能需要返回新的 refreshToken 轮换标志）</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>用户 accessToken 过期后，通过 refreshToken 换取新的令牌对</li>
 *   <li>可实现 refreshToken 的自动轮换（每次刷新下发新的 refreshToken）</li>
 *   <li>在此场景中，通常不需要返回用户详细信息，因为客户端已有缓存</li>
 * </ul>
 *
 * <p><b>注意：</b>
 * 此对象保持极简设计，只包含令牌数据。
 * Web 层的响应对象（如 RefreshResponse）负责做协议适配，
 * 如需添加 token_type、过期时间格式化等前端友好字段，应在 Web 层完成。
 *
 * @author loriyuhv
 * @version 1.0 2026/8/7 15:33
 * @since 1.0
 */
@Data
@Builder
public class RefreshResult {

    /** 新的访问令牌（Access Token），用于后续接口调用的身份凭证 */
    private String accessToken;

    /**
     * 新的刷新令牌（Refresh Token）；
     * 安全策略设计：轮换策略
     */
    private String refreshToken;

    /** 新 accessToken 的有效期，单位：秒（例如 7200 表示 2 小时） */
    private long expiresIn;

}
