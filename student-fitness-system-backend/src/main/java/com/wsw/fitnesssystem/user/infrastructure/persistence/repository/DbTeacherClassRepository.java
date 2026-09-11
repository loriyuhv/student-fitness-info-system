package com.wsw.fitnesssystem.user.infrastructure.persistence.repository;

import com.wsw.fitnesssystem.user.domain.repository.TeacherClassRepository;
import com.wsw.fitnesssystem.user.infrastructure.persistence.mapper.TeacherClassMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * 教师-班级关系仓储实现（Infrastructure 层）。
 * <p>
 * 实现 Domain 层的 {@link TeacherClassRepository}，
 * 委托 {@link TeacherClassMapper} 完成 SQL 查询。
 * </p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/12 00:33
 * @since 1.0
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class DbTeacherClassRepository implements TeacherClassRepository {

    private final TeacherClassMapper teacherClassMapper;

    @Override
    public Set<Long> findClassIdsByUserIdAndCampusId(Long userId, Long campusId) {
        if (userId == null || campusId == null) {
            log.debug("Skip teacher class query for null userId or campusId");
            return Set.of();
        }

        Set<Long> classIds = teacherClassMapper.selectClassIdsByUserIdAndCampusId(userId, campusId);

        return classIds != null ? classIds : Set.of();
    }
}
