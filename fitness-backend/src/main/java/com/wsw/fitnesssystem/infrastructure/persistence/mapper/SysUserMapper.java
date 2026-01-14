package com.wsw.fitnesssystem.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsw.fitnesssystem.infrastructure.persistence.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:54
 * @since 1.0
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0;")
    SysUser selectByUsername(@Param("username") String username);

    @Select("SELECT DISTINCT p.perm_code " +
        "FROM sys_user_role ur " +
        "JOIN sys_role r" +
        "                      ON ur.role_id = r.role_id" +
        "                          AND r.deleted = 0" +
        "                          AND r.status = 1" +
        "                 JOIN sys_role_permission rp" +
        "                      ON r.role_id = rp.role_id" +
        "                          AND rp.deleted = 0" +
        "                          AND rp.status = 1" +
        "                 JOIN sys_permission p" +
        "                      ON rp.perm_id = p.perm_id" +
        "                          AND p.deleted = 0" +
        "                          AND p.status = 1" +
        "        WHERE ur.user_id = #{userId}" +
        "          AND ur.deleted = 0" +
        "          AND ur.status = 1")
    Set<String> selectPermissionsByUserId(@Param("userId") Long userId);
}
