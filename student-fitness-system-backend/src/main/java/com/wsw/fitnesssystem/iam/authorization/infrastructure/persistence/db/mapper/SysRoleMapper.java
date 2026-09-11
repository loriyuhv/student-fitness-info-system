package com.wsw.fitnesssystem.iam.authorization.infrastructure.persistence.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsw.fitnesssystem.iam.authorization.infrastructure.persistence.db.entity.SysRole;
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

    /**
     * 查询用户所有启用角色的最小 data_scope。
     * <p>取最小值的原因：data_scope 数字越小权限越大
     * （0-全部数据权限最大，1-仅本人，2-本班，3-本学院）。</p>
     * <p>使用 {@code MIN()} 聚合，若无记录返回 {@code NULL}。</p>
     *
     * @param userId   用户 ID
     * @param campusId 校区 ID
     * @return 最小 data_scope，无角色时返回 {@code null}
     */
    @Select("""
            SELECT MIN(r.data_scope) FROM sys_user_role ur
            INNER JOIN sys_role r ON ur.role_id = r.role_id AND r.campus_id = #{campusId} AND r.status = 1 AND r.deleted = 0
            WHERE ur.user_id = #{userId}
              AND ur.status = 1
              AND ur.deleted = 0
        """
    )
    Integer selectMinDataScopeByUserIdAndCampusId(
        @Param("userId") Long userId, @Param("campusId") Long campusId
    );

}
