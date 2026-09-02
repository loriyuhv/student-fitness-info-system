package com.wsw.fitnesssystem.user.domain.model;

import com.wsw.fitnesssystem.user.domain.valueobject.Gender;
import com.wsw.fitnesssystem.user.domain.valueobject.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生扩展信息
 *
 * @author loriyuhv
 * @version 1.0 2026/9/2 08:54
 * @since 1.0
 */
@Setter
@Getter
@Builder
public class StudentProfile {

    private Long studentId;
    private Long campusId;
    private Long userId;
    private String studentNo;
    private Long classId;
    private Integer enrollYear;
    private String major;
    private String idCard;
    private Gender gender;
    private LocalDate birthDate;
    private String familyAddress;
    private String avatarUrl;
    private String remark;
    private Status status;
    private boolean deleted;
    private Long createBy;
    private Long updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
