package com.wsw.fitnesssystem.auth.authorization.infrastructure.persistence.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsw.fitnesssystem.auth.authorization.infrastructure.persistence.db.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:54
 * @since 1.0
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    /**
     * 根据用户账号查询用户信息
     * @param username 用户账号
     * @return SysUser
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0;")
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 判断用户是否存在（根据 campusId + userId）
     */
    @Select("SELECT COUNT(1) FROM sys_user WHERE user_id = #{userId} AND campus_id = #{campusId} AND deleted = 0")
    int existsByCampusAndId(@Param("campusId") Long campusId, @Param("userId") Long userId);

    /**
     * 根据用户ID和校区ID查询
     * @param userId 用户ID
     * @param campusId 校区ID
     * @return SysUser
     */
    @Select("SELECT * FROM sys_user WHERE user_id = #{userId} AND campus_id = #{campusId};")
    SysUser selectByUserIdAndCampusId(@Param("userId") Long userId, @Param("campusId") Long campusId);
}
