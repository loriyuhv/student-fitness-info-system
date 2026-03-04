package com.wsw.fitnesssystem.auth.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户实体
 * 对应表：sys_user
 * 注意：
 * - MyBatis-Plus 持久化对象/实体（PO / Entity）
 * - 不直接返回给前端
 * - 不承载业务逻辑
 *
 * @author loriyuhv
 * @version 1.0 2026/1/11 8:28
 * @since 1.0
 */
@Data
@TableName("sys_user") // 启用 MP 代理
public class SysUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long userId;

    /** 登录账号（学号 / 工号） */
    private String username;

    /** 登录密码（BCrypt 等加密） */
    private String password;

    /** 昵称 */
    private String nickName;

    /** 手机号 */
    private String phoneNumber;

    /** 邮箱 */
    private String email;

    /** 备注 */
    private String remark;

    /** 来源 */
    private String source;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 逻辑删除：0-未删除，1-已删除 */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    /** 创建时间：只在插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间：插入和更新时都填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
