package com.wsw.fitnesssystem.user.interfaces.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wsw.fitnesssystem.user.application.dto.result.UserListItemResult;
import lombok.Builder;
import lombok.Data;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/14 18:24
 * @since 1.0
 */
@Data
@Builder
public class UserListItemResponse {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("campus_id")
    private Long campusId;

    private String username;

    @JsonProperty("user_type")
    private Integer userType;

    private Integer status;

    private String nickname;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String email;

    // 学生专属（userType=2 时有值）
    @JsonProperty("student_no")
    private String studentNo;

    @JsonProperty("class_id")
    private Long classId;

    private String major;

    // 教师专属（userType=1 时有值）
    @JsonProperty("teacher_no")
    private String teacherNo;

    public static UserListItemResponse from(UserListItemResult r) {
        return UserListItemResponse.builder()
            .userId(r.getUserId())
            .campusId(r.getCampusId())
            .username(r.getUsername())
            .userType(r.getUserType())
            .status(r.getStatus())
            .nickname(r.getNickname())
            .phoneNumber(r.getPhoneNumber())
            .email(r.getEmail())
            .studentNo(r.getStudentNo())
            .classId(r.getClassId())
            .major(r.getMajor())
            .teacherNo(r.getTeacherNo())
            .build();
    }

}
