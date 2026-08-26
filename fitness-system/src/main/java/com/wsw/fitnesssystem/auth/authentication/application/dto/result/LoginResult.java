package com.wsw.fitnesssystem.auth.authentication.application.dto.result;

import lombok.Builder;
import lombok.Data;

/**
 * 登录业务输出模型（Application 层纯数据对象）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>承载应用层登录认证后的业务结果数据</li>
 *   <li>作为 Application Service 的返回值，供 Controller 或 MQ 消费者等调用方使用</li>
 *   <li><b>严禁</b>添加任何 Web/JSON 序列化注解（如 @JsonProperty、@JsonFormat），
 *       保持 POJO 的纯净性，确保其在非 Web 环境（如 RPC、定时任务）中可独立复用</li>
 * </ul>
 *
 * <p><b>注意：</b>
 * <ul>
 *     <li>此对象只包含业务核心数据，不包含前端协议适配信息。</li>
 *     <li>前端字段名变更（如 accessToken -> token）应由 Web 层的 LoginResponse 处理，
 *     严禁为了迁就前端而修改此类的字段名。</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:21
 * @since 1.0
 */
@Data
@Builder
public class LoginResult {

    /** 访问令牌（Access Token），用于后续请求的身份凭证 */
    private String accessToken;

    /** 刷新令牌（Refresh Token），用于在 Access Token 过期后获取新的令牌对 */
    private String refreshToken;

    /** Access Token 的有效期，单位：秒（例如 7200 表示 2 小时） */
    private long expiresIn;

}
