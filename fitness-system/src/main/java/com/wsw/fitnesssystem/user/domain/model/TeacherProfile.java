package com.wsw.fitnesssystem.user.domain.model;

import com.wsw.fitnesssystem.user.domain.valueobject.Gender;
import com.wsw.fitnesssystem.user.domain.valueobject.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 教师扩展信息
 *
 * @author loriyuhv
 * @version 1.0 2026/9/2 08:55
 * @since 1.0
 */
@Setter
@Getter
@Builder
public class TeacherProfile {

    private Long teacherId;
    private Long campusId;
    private Long userId;
    private String teacherNo;
    private Gender gender;
    private String remark;
    private Status status;
    private boolean deleted;
    private Long createBy;
    private Long updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
