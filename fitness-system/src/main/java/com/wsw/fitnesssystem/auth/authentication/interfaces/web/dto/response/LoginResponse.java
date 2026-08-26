package com.wsw.fitnesssystem.auth.authentication.interfaces.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 登录响应 DTO（Web 层面向 HTTP 协议的输出模型）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>适配前端 HTTP 响应格式，处理字段命名、日期格式、空值策略等协议层需求</li>
 *   <li>作为 Controller 的返回值，被 Spring MVC 自动序列化为 JSON</li>
 *   <li><b>允许</b>使用 Jackson 注解（@JsonProperty、@JsonFormat 等）进行协议适配</li>
 * </ul>
 *
 * <p><b>与 LoginResult 的关系：</b>
 * <ul>
 *   <li>虽然字段与 LoginResult 几乎相同，但职责完全不同</li>
 *   <li>Controller 负责将 LoginResult 显式转换为 LoginResponse，
 *       实现“业务数据”与“协议展示”的隔离</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 10:02
 * @since 1.0
 */
@Data
@Builder
public class LoginResponse {

    /**
     * 访问令牌
     * 前端要求字段名为 "access_token"（蛇形命名），
     * 通过 @JsonProperty 适配，而不修改核心字段名
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * 刷新令牌
     * 前端要求字段名为 "refresh_token"
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * 有效期（秒）
     * 前端要求字段名为 "expires_in"
     */
    @JsonProperty("expires_in")
    private Long expiresIn;

}
