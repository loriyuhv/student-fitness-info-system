package com.wsw.fitnesssystem.user.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教师信息扩展表实体
 * 对应表：teacher_profile
 *
 * @author loriyuhv
 * @version 1.0 2026/9/2 08:49
 * @since 1.0
 */
@Data
@TableName("teacher_profile")
public class TeacherProfilePo {

    @TableId(type = IdType.AUTO)
    private Long teacherId;

    @TableField("campus_id")
    private Long campusId;

    @TableField("user_id")
    private Long userId;

    @TableField("teacher_no")
    private String teacherNo;

    private Integer gender;

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
