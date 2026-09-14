package com.wsw.fitnesssystem.user.interfaces.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/14 18:24
 * @since 1.0
 */
@Data
@Builder
public class StudentListItemResponse {

    @JsonProperty("student_id")
    private Long studentId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("student_no")
    private String studentNo;

    @JsonProperty("class_id")
    private Long classId;

    @JsonProperty("enroll_year")
    private Integer enrollYear;

    private String major;

    private Integer gender;

    @JsonProperty("family_address")
    private String familyAddress;

    private String nickname;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String email;

}
