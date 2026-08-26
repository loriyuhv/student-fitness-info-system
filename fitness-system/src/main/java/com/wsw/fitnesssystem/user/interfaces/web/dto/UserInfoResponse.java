package com.wsw.fitnesssystem.user.interfaces.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 用户信息响应 DTO（Web 层面向 HTTP 协议）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>适配前端对用户信息接口的响应格式</li>
 *   <li>支持字段命名转换（如 user_id）</li>
 *   <li>允许根据前端需求灵活调整字段，不影响 Application 层</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 16:09
 * @since 1.0
 */
@Data
@Builder
public class UserInfoResponse {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("campus_id")
    private Long campusId;

    private String username;
    private String nickname;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String email;
    private String remark;

    @JsonProperty("user_type")
    private Integer userType;

}
