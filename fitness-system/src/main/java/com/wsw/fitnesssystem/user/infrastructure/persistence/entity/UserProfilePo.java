package com.wsw.fitnesssystem.user.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

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
@Data
@TableName("user_profile")
public class UserProfilePo {

    @TableId(type = IdType.AUTO)
    private Long profileId;

    @TableField("campus_id")
    private Long campusId;

    @TableField("user_id")
    private Long userId;

    private Integer gender;

    @TableField("birth_date")
    private LocalDate birthDate;

    @TableField("avatar_url")
    private String avatarUrl;

    private String address;

    @TableField("last_login_ip")
    private String lastLoginIp;

    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
