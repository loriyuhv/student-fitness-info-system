package com.wsw.fitnesssystem.user.domain.port;

import com.wsw.fitnesssystem.user.domain.model.TeacherProfile;

import java.util.Optional;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 09:01
 * @since 1.0
 */
public interface TeacherProfileRepository {

    /**
     * 根据用户ID和校区ID查询教师信息
     */
    Optional<TeacherProfile> findByUserIdAndCampusId(Long userId, Long campusId);

    /**
     * 根据工号查询教师信息
     */
    Optional<TeacherProfile> findByTeacherNo(String teacherNo);

    /**
     * 保存教师信息
     */
    void save(TeacherProfile teacher);

}
