package com.wsw.fitnesssystem.user.domain.repository;

import java.util.Set;

/**
 * 教师-班级关系仓储接口（Domain 层 Port）。
 * <p>
 * 定义"查询教师任教班级"的契约，由 Infrastructure 层实现。
 * 适配器 {@code TeacherClassQueryAdapter} 通过本接口访问数据，
 * 不直接依赖 MyBatis Mapper，保证领域层与技术实现解耦。
 * </p>
 *
 * <p><b>数据来源：</b>{@code teacher_profile} JOIN {@code teacher_class}。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/12 00:28
 * @since 1.0
 */
public interface TeacherClassRepository {

    /**
     * 查询教师任教的所有有效班级 ID。
     * <p>
     * 过滤条件：教师档案有效（status=1、deleted=0）、关联关系有效（status=1、deleted=0），
     * 且教师与班级同属一个校区。
     * </p>
     *
     * @param userId   教师对应的用户 ID（{@code sys_user.user_id}）
     * @param campusId 校区 ID
     * @return 班级 ID 集合；若用户非教师、无任教班级或参数无效，返回空集合
     */
    Set<Long> findClassIdsByUserIdAndCampusId(Long userId, Long campusId);

}
