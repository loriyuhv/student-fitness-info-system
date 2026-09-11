package com.wsw.fitnesssystem.user.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

/**
 * 教师-班级关系 Mapper（Infrastructure 层）。
 * <p>
 * 仅提供自定义查询，不继承 {@code BaseMapper}，因为本任务只涉及班级 ID 查询，
 * 无实体 CRUD 需求。若未来做班级管理（分配/撤销/查询关联详情），
 * 可扩展为继承 {@code BaseMapper<TeacherClassPo>} 并引入 PO。
 * </p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/12 00:30
 * @since 1.0
 */
@Mapper
public interface TeacherClassMapper {

    /**
     * 查询教师任教的所有有效班级 ID。
     *
     * <p><b>SQL 逻辑：</b></p>
     * <ol>
     *   <li>通过 {@code teacher_profile.user_id} 定位教师档案</li>
     *   <li>通过 {@code teacher_class.teacher_id} 关联任教班级</li>
     *   <li>过滤教师档案与关联关系的 status/deleted</li>
     *   <li>双重校验 campus_id，确保跨校区隔离</li>
     * </ol>
     *
     * @param userId   教师对应的用户 ID
     * @param campusId 校区 ID
     * @return 班级 ID 集合（可能为空集合）
     */
    @Select("""
        SELECT DISTINCT tc.class_id
        FROM teacher_class tc
        INNER JOIN teacher_profile tp ON tc.teacher_id = tp.teacher_id
        WHERE tp.user_id = #{userId}
          AND tp.campus_id = #{campusId}
          AND tp.status = 1
          AND tp.deleted = 0
          AND tc.campus_id = #{campusId}
          AND tc.status = 1
          AND tc.deleted = 0
        """)
    Set<Long> selectClassIdsByUserIdAndCampusId(
        @Param("userId") Long userId,
        @Param("campusId") Long campusId
    );

}
