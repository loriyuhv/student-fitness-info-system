package com.wsw.fitnesssystem.user.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生信息扩展表实体
 * 对应表：student_profile
 *
 * @author loriyuhv
 * @version 1.0 2026/9/2 08:37
 * @since 1.0
 */
@Data
@TableName("student_profile")
public class StudentProfilePo {

    @TableId(type = IdType.AUTO)
    private Long studentId;

    @TableField("campus_id")
    private Long campusId;

    @TableField("user_id")
    private Long userId;

    @TableField("student_no")
    private String studentNo;

    @TableField("class_id")
    private Long classId;

    @TableField("enroll_year")
    private Integer enrollYear;

    private String major;

    @TableField("id_card")
    private String idCard;

    private Integer gender;

    @TableField("birth_date")
    private LocalDate birthDate;

    @TableField("family_address")
    private String familyAddress;

    @TableField("avatar_url")
    private String avatarUrl;

    private Integer status;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
