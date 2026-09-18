package com.wsw.fitnesssystem.user.domain.repository;

import com.wsw.fitnesssystem.user.domain.model.TeacherProfile;

import java.util.Collection;
import java.util.List;
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

    Optional<TeacherProfile> findByUserId(Long userId);

    /**
     * 按用户 ID 集合批量查询（避免 N+1）。
     *
     * @param userIds 用户 ID 集合；为空返回空列表
     */
    List<TeacherProfile> findByUserIds(Collection<Long> userIds);

    /**
     * 保存教师信息
     */
    void save(TeacherProfile teacher);

}
