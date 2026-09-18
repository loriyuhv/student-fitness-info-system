package com.wsw.fitnesssystem.user.domain.repository;

import com.wsw.fitnesssystem.shared.response.PageResult;
import com.wsw.fitnesssystem.user.domain.model.StudentProfile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 09:00
 * @since 1.0
 */
public interface StudentProfileRepository {

    // ==================== 单条查询（保留） ====================

    /**
     * 根据用户ID和校区ID查询学生信息
     */
    Optional<StudentProfile> findByUserIdAndCampusId(Long userId, Long campusId);

    /**
     * 根据学号查询学生信息
     */
    Optional<StudentProfile> findByStudentNo(String studentNo);


    Optional<StudentProfile> findByUserId(Long userId);

    /**
     * 按用户 ID 集合批量查询（避免 N+1）。
     *
     * @param userIds 用户 ID 集合；为空返回空列表
     */
    List<StudentProfile> findByUserIds(Collection<Long> userIds);

    // ==================== 写入（保留） ====================

    /**
     * 保存学生信息
     */
    void save(StudentProfile student);

    // ==================== 分页查询（新增） ====================

    /**
     * 分页查询学生档案。
     *
     * <p><b>数据权限：</b>由 {@code DataPermissionAspect} 装配，
     * {@code CustomDataPermissionHandler} 在 SQL 层自动追加过滤条件。
     * 本方法<b>不主动</b>过滤任何业务字段（campusId / userId / classId）。</p>
     *
     * <p><b>固定过滤：</b>{@code status=1} 和 {@code deleted=0}（业务语义，非数据权限）。</p>
     */
    PageResult<StudentProfile> page(int pageNum, int pageSize);

}
