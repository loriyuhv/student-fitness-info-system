package com.wsw.fitnesssystem.user.interfaces.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 用户列表项响应（Web 出参）。
 *
 * <p>通用字段 + 按 userType 二选一的扩展字段，与 {@code UserListItemResult} 字段一一对应。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 18:24
 * @since 1.0
 */
public record UserListItemResponse(
    @JsonProperty("user_id") Long userId,
    @JsonProperty("campus_id") Long campusId,
    String username,
    @JsonProperty("user_type") Integer userType,
    Integer status,
    String nickname,
    @JsonProperty("phone_number") String phoneNumber,
    String email,
    // 学生专属（userType=2 时有值）
    @JsonProperty("student_no") String studentNo,
    @JsonProperty("class_id") Long classId,
    String major,
    // 教师专属（userType=1 时有值）
    @JsonProperty("teacher_no") String teacherNo
) {
}
