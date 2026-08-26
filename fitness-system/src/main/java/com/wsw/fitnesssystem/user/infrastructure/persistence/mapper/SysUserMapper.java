package com.wsw.fitnesssystem.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.UserPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:54
 * @since 1.0
 */
@Mapper
public interface SysUserMapper extends BaseMapper<UserPo> {

    /**
     * 根据用户账号查询用户信息
     * @param username 用户账号
     * @return UserPo
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0;")
    UserPo selectByUsername(@Param("username") String username);

    /**
     * 根据用户ID和校区ID查询
     * @param campusId 校区ID
     * @param userId 用户ID
     * @return UserPo
     */
    @Select("SELECT * FROM sys_user WHERE campus_id = #{campusId} AND user_id = #{userId} AND deleted =  0;")
    UserPo selectByCampusIdAndUserId(@Param("campusId") Long campusId, @Param("userId") Long userId);

}
