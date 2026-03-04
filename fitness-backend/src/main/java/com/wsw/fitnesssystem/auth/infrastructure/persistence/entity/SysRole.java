package com.wsw.fitnesssystem.auth.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统角色表实体类
 *
 * @author loriyuhv
 * @version 1.0 2026/1/15 13:13
 * @since 1.0
 */
@Data
@TableName("sys_role")
public class SysRole implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;

    /**
     * 角色编码（系统唯一）
     */
    @TableField("role_code")
    private String roleCode;

    /**
     * 角色名称（业务唯一）
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态（0-禁用，1-启用），业务可见性
     */
    private Integer status;

    /**
     * 逻辑删除（0-未删除 1-已删除），数据可见性
     */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
