package com.wsw.fitnesssystem.auth.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.entity.SysRole;
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
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /**
     * 根据用户ID查询角色列表
     */
    @Select("""
        SELECT DISTINCT r.* FROM sys_user_role ur
        INNER JOIN sys_role r ON ur.role_id = r.role_id
        WHERE ur.user_id = #{userId}
          AND ur.status = 1
          AND ur.deleted = 0
          AND r.status = 1
          AND r.deleted = 0
        ORDER BY r.create_time DESC
    """)
    Set<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询角色编码列表
     */
    @Select("""
        SELECT DISTINCT r.role_code FROM sys_user_role ur
        INNER JOIN sys_role r ON ur.role_id = r.role_id
        WHERE ur.user_id = #{userId}
          AND ur.status = 1
          AND ur.deleted = 0
          AND r.status = 1
          AND r.deleted = 0
    """)
    Set<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
