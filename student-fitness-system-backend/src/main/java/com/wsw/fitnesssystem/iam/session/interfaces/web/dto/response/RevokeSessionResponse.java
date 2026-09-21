package com.wsw.fitnesssystem.iam.session.interfaces.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

/**
 * 撤销会话响应 DTO（Web 层面向 HTTP 协议的输出模型）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>适配前端 HTTP 响应格式，处理字段命名（蛇形）、展示消息等协议层需求</li>
 *   <li>作为 {@code AdminSessionController#revokeUserSessions} 的返回值</li>
 *   <li><b>允许</b>使用 Jackson 注解做协议适配</li>
 * </ul>
 *
 * <p><b>与 RevokeSessionResult 的关系：</b>
 * <ul>
 *   <li>{@code RevokeSessionResult} 是 Application 层返回的纯业务对象（无任何 JSON 注解）</li>
 *   <li>{@code RevokeSessionResponse} 是 Web 层的协议适配对象</li>
 *   <li>Controller 通过 Assembler 完成转换，实现 ACL</li>
 * </ul>
 *
 * @param revokedCount 被撤销的会话数量
 * @param tokenIds     被撤销的 AccessTokenId 列表
 * @param message      面向用户展示的结果消息
 * @author loriyuhv
 * @version 1.0 2026/9/21 15:49
 * @since 1.0
 */
public record RevokeSessionResponse(
    @JsonProperty("revoked_count")
    int revokedCount,
    @JsonProperty("token_ids")
    Set<String> tokenIds,
    String message
) {
}
