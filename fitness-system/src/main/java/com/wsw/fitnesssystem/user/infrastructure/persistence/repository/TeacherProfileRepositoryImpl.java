package com.wsw.fitnesssystem.user.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsw.fitnesssystem.user.domain.model.TeacherProfile;
import com.wsw.fitnesssystem.user.domain.port.TeacherProfileRepository;
import com.wsw.fitnesssystem.user.infrastructure.persistence.converter.TeacherProfileConverter;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.TeacherProfilePo;
import com.wsw.fitnesssystem.user.infrastructure.persistence.mapper.TeacherProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 09:03
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class TeacherProfileRepositoryImpl implements TeacherProfileRepository {

    private final TeacherProfileMapper mapper;
    private final TeacherProfileConverter converter;

    @Override
    public Optional<TeacherProfile> findByUserIdAndCampusId(Long userId, Long campusId) {
        TeacherProfilePo po = mapper.selectOne(
            new LambdaQueryWrapper<TeacherProfilePo>()
                .eq(TeacherProfilePo::getUserId, userId)
                .eq(TeacherProfilePo::getCampusId, campusId)
                .eq(TeacherProfilePo::getDeleted, 0)
        );
        return Optional.ofNullable(po).map(converter::toDomain);
    }

    @Override
    public Optional<TeacherProfile> findByTeacherNo(String teacherNo) {
        TeacherProfilePo po = mapper.selectOne(
            new LambdaQueryWrapper<TeacherProfilePo>()
                .eq(TeacherProfilePo::getTeacherNo, teacherNo)
                .eq(TeacherProfilePo::getDeleted, 0)
        );
        return Optional.ofNullable(po).map(converter::toDomain);
    }

    @Override
    public void save(TeacherProfile teacher) {
        TeacherProfilePo po = converter.toPo(teacher);
        if (po.getTeacherId() == null) {
            mapper.insert(po);
            teacher.setTeacherId(po.getTeacherId());
        } else {
            mapper.updateById(po);
        }
    }

}
