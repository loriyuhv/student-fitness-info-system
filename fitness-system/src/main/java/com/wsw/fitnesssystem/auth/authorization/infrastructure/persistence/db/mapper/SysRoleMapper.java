package com.wsw.fitnesssystem.auth.authorization.infrastructure.persistence.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsw.fitnesssystem.auth.authorization.infrastructure.persistence.db.entity.SysRole;
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
     * 根据用户ID和校区ID查询角色编码集合
     */
    @Select("""
        SELECT DISTINCT r.role_code FROM sys_user_role ur
        INNER JOIN sys_role r ON ur.role_id = r.role_id
        WHERE ur.user_id = #{userId}
          AND r.campus_id = #{campusId}
          AND ur.status = 1
          AND ur.deleted = 0
          AND r.status = 1
          AND r.deleted = 0
    """)
    Set<String> selectRoleCodesByUserIdAndCampusId(
        @Param("userId") Long userId, @Param("campusId") Long campusId
    );

}
