package com.wsw.fitnesssystem.user.interfaces.web.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * A2 请求：修改本人画像。
 *
 * <p><b>字段语义：</b></p>
 * <ul>
 *   <li>所有字段可选；为 null 表示"不修改该字段"</li>
 *   <li>空串表示"清空该字段"（仅对可空字段有效）</li>
 * </ul>
 *
 * <p><b>禁止字段：</b>username、userType、campusId、studentNo、idCard、teacherNo
 * 均不在此 DTO 中，服务端按字段白名单处理。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/18 08:53
 * @since 1.0
 */
@Data
public class UpdateMyProfileRequest {

    private String nickname;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String email;

    private Integer gender;

    @JsonProperty("birth_date")
    private LocalDate birthDate;

    private String address;

    @JsonProperty("avatar_url")
    private String avatarUrl;

}
