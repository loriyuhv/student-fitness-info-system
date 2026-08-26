package com.wsw.fitnesssystem.auth.authentication.interfaces.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 刷新令牌响应 DTO（Web 层面向 HTTP 协议的输出模型）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>适配前端 HTTP 响应格式，处理字段命名（蛇形转驼峰）等协议层需求</li>
 *   <li>作为 Controller 刷新接口的返回值，被 Spring MVC 自动序列化为 JSON</li>
 *   <li><b>允许</b>使用 Jackson 注解做协议适配，与 Application 层的纯净 POJO 隔离</li>
 * </ul>
 *
 * <p><b>与 RefreshResult 的关系：</b>
 * <ul>
 *   <li>{@code RefreshResult} 是 Application 层返回的纯业务对象（无任何 JSON 注解）</li>
 *   <li>{@code RefreshResponse} 是 Web 层的协议适配对象（含 @JsonProperty 等）</li>
 *   <li>Controller 负责将前者显式转换为后者，实现防腐层（ACL）模式</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 10:28
 * @since 1.0
 */
@Data
@Builder
public class RefreshResponse {

    /**
     * 新的访问令牌
     * 前端约定字段名为 "access_token"（蛇形命名），通过 @JsonProperty 适配
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * 新的刷新令牌
     * 前端约定字段名为 "refresh_token"
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * 新 accessToken 的有效期（单位：秒）
     * 前端约定字段名为 "expires_in"
     */
    @JsonProperty("expires_in")
    private Long expiresIn;

}
