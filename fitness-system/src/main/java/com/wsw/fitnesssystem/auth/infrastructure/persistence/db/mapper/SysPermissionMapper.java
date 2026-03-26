package com.wsw.fitnesssystem.auth.infrastructure.persistence.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.db.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/15 13:24
 * @since 1.0
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    /**
     * 根据用户ID查询权限列表
     */
    @Select("""
        SELECT DISTINCT p.* FROM sys_user_role ur
        INNER JOIN sys_role r ON ur.role_id = r.role_id
        INNER JOIN sys_role_permission rp ON r.role_id = rp.role_id
        INNER JOIN sys_permission p ON rp.perm_id = p.perm_id
        WHERE ur.user_id = #{userId}
          AND ur.status = 1
          AND ur.deleted = 0
          AND r.status = 1
          AND r.deleted = 0
          AND rp.status = 1
          AND rp.deleted = 0
          AND p.status = 1
          AND p.deleted = 0
        ORDER BY p.create_time DESC
    """)
    Set<SysPermission> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询权限编码集合
     */
    @Select("""
        SELECT DISTINCT p.perm_code FROM sys_user_role ur
        INNER JOIN sys_role r ON ur.role_id = r.role_id
        INNER JOIN sys_role_permission rp ON r.role_id = rp.role_id
        INNER JOIN sys_permission p ON rp.perm_id = p.perm_id
        WHERE ur.user_id = #{userId}
          AND ur.status = 1
          AND ur.deleted = 0
          AND r.status = 1
          AND r.deleted = 0
          AND rp.status = 1
          AND rp.deleted = 0
          AND p.status = 1
          AND p.deleted = 0
    """)
    Set<String> selectPermCodesByUserId(@Param("userId") Long userId);
}
