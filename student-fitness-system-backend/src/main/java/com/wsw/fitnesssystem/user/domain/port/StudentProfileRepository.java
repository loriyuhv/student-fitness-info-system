package com.wsw.fitnesssystem.user.domain.port;

import com.wsw.fitnesssystem.user.domain.model.StudentProfile;

import java.util.Optional;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 09:00
 * @since 1.0
 */
public interface StudentProfileRepository {

    /**
     * 根据用户ID和校区ID查询学生信息
     */
    Optional<StudentProfile> findByUserIdAndCampusId(Long userId, Long campusId);

    /**
     * 根据学号查询学生信息
     */
    Optional<StudentProfile> findByStudentNo(String studentNo);

    /**
     * 保存学生信息
     */
    void save(StudentProfile student);

}
