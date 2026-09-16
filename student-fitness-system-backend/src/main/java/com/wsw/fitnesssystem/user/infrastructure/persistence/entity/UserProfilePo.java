package com.wsw.fitnesssystem.user.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户信息扩展表实体
 * 对应表：user_profile
 *
 * @author loriyuhv
 * @version 1.0 2026/9/2 08:35
 * @since 1.0
 */
@Setter
@Getter
@TableName("user_profile")
public class UserProfilePo {

    @TableId(value = "profile_id", type = IdType.AUTO)
    private Long profileId;

    @TableField("user_id")
    private Long userId;

    @TableField("campus_id")
    private Long campusId;

    @TableField("nickname")
    private String nickname;

    @TableField("phone_number")
    private String phoneNumber;

    @TableField("email")
    private String email;

    @TableField("gender")
    private Integer gender;

    @TableField("birth_date")
    private LocalDate birthDate;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("address")
    private String address;

    @TableField("remark")
    private String remark;

    @TableField("deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
