package com.wsw.fitnesssystem.user.interfaces.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wsw.fitnesssystem.user.application.dto.result.AdminUserDetailResult;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/18 09:38
 * @since 1.0
 */
@Data
@Builder
public class AdminUserDetailResponse {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("campus_id")
    private Long campusId;

    private String username;

    @JsonProperty("user_type")
    private Integer userType;

    private Integer source;
    private Integer status;

    private String nickname;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String email;
    private Integer gender;

    @JsonProperty("birth_date")
    private LocalDate birthDate;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String address;
    private String remark;

    // 扩展（按 userType 二选一）
    @JsonProperty("student_profile")
    private StudentBlock student;

    @JsonProperty("teacher_profile")
    private TeacherBlock teacher;

    @Data
    @Builder
    public static class StudentBlock {
        @JsonProperty("student_no")
        private String studentNo;
        @JsonProperty("class_id")
        private Long classId;
        @JsonProperty("enroll_year")
        private Integer enrollYear;
        private String major;
        @JsonProperty("id_card_masked")
        private String idCardMasked;
        @JsonProperty("family_address")
        private String familyAddress;
    }

    @Data
    @Builder
    public static class TeacherBlock {
        @JsonProperty("teacher_no")
        private String teacherNo;
        private String remark;
    }

    /** 从 Application 层结果转换为 Web 响应 */
    public static AdminUserDetailResponse from(AdminUserDetailResult r) {
        AdminUserDetailResponseBuilder b = AdminUserDetailResponse.builder()
            .userId(r.getUserId())
            .campusId(r.getCampusId())
            .username(r.getUsername())
            .userType(r.getUserType())
            .source(r.getSource())
            .status(r.getStatus())
            .nickname(r.getNickname())
            .phoneNumber(r.getPhoneNumber())
            .email(r.getEmail())
            .gender(r.getGender())
            .birthDate(r.getBirthDate())
            .avatarUrl(r.getAvatarUrl())
            .address(r.getAddress())
            .remark(r.getRemark());

        if (r.getStudent() != null) {
            var s = r.getStudent();
            b.student(StudentBlock.builder()
                .studentNo(s.getStudentNo())
                .classId(s.getClassId())
                .enrollYear(s.getEnrollYear())
                .major(s.getMajor())
                .idCardMasked(s.getIdCardMasked())
                .familyAddress(s.getFamilyAddress())
                .build());
        }
        if (r.getTeacher() != null) {
            var t = r.getTeacher();
            b.teacher(TeacherBlock.builder()
                .teacherNo(t.getTeacherNo())
                .remark(t.getRemark())
                .build());
        }
        return b.build();
    }

}
