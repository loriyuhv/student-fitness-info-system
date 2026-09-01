package com.wsw.fitnesssystem.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.UserPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:54
 * @since 1.0
 */
@Mapper
public interface SysUserMapper extends BaseMapper<UserPo> {

    /**
     * 根据用户名称查询
     *
     * @param username 用户名称
     * @return UserPo
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0;")
    UserPo selectByUsername(@Param("username") String username);

    /**
     * 根据校区ID和用户ID查询
     *
     * @param campusId 校区ID
     * @param userId 用户ID
     * @return UserPo
     */
    @Select("SELECT * FROM sys_user WHERE campus_id = #{campusId} AND user_id = #{userId} AND deleted =  0;")
    UserPo selectByCampusIdAndUserId(@Param("campusId") Long campusId, @Param("userId") Long userId);

    /**
     * 批量查询已存在的用户名（用于导入查重）
     * @param usernames 用户名列表
     * @return 匹配的用户名列表
     */
    List<String> selectExistingUsernames(@Param("usernames") List<String> usernames);

    /**
     * 批量插入用户
     * @param list User实体列表
     * @return 成功行数
     */
    int batchInsert(@Param("list") List<UserPo> list);

}
