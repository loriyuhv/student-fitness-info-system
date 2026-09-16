package com.wsw.fitnesssystem.iam.authentication.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * sys_user 表持久化对象
 *
 * @author loriyuhv
 * @version 1.0 2026/9/16 14:48
 * @since 1.0
 */
@Setter
@Getter
@TableName("sys_user")
public class SysUserPo {

    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 校区ID
     */
    @TableField("campus_id")
    private Long campusId;

    /**
     * 登录账号（学号 / 工号）
     */
    @TableField("username")
    private String username;

    /**
     * 登录密码（BCrypt 等加密）
     */
    @TableField("password")
    private String password;

    /**
     * 用户类型：0-管理员 1-教师 2-学生
     */
    @TableField("user_type")
    private Integer userType;

    /**
     * 来源
     */
    @TableField("source")
    private Integer source;

    /**
     * 状态：0-禁用，1-启用（业务可见性）
     */
    @TableField("status")
    private Integer status;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableField("deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    /**
     * 创建人ID：只在插入时自动填充
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建时间：只在插入时自动填充
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人ID：插入和更新时都填充
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新时间：插入和更新时都填充
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
