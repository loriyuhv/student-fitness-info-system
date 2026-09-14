package com.wsw.fitnesssystem.user.domain.repository;

import com.wsw.fitnesssystem.shared.response.PageResult;
import com.wsw.fitnesssystem.user.domain.model.StudentProfile;

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

    // ==================== 写入（保留） ====================

    /**
     * 保存学生信息
     */
    void save(StudentProfile student);

    // ==================== 分页查询（新增） ====================

    /**
     * 按校区分页查询在籍学生档案。
     *
     * <p><b>当前实现：</b>仅按 {@code campus_id} 过滤，配合 {@code status=1}、{@code deleted=0}。</p>
     *
     * <p><b>⚠️ 临时方案：</b>本方法暂未接入数据权限拦截器，
     * 调用方需自行保证 {@code campusId} 的合法性。
     * 待 {@code DataPermissionSqlBuilder} 补全后，
     * 本方法将改为 {@code page(pageNum, pageSize)}，
     * 由拦截器自动追加 {@code campus_id} 和 scope 条件。</p>
     *
     * @param campusId 校区 ID
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<StudentProfile> pageByCampusId(Long campusId, int pageNum, int pageSize);

}
