package com.wsw.fitnesssystem.user.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsw.fitnesssystem.user.domain.model.StudentProfile;
import com.wsw.fitnesssystem.user.domain.port.StudentProfileRepository;
import com.wsw.fitnesssystem.user.infrastructure.persistence.converter.StudentProfileConverter;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.StudentProfilePo;
import com.wsw.fitnesssystem.user.infrastructure.persistence.mapper.StudentProfileMapper;
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
public class StudentProfileRepositoryImpl implements StudentProfileRepository {

    private final StudentProfileMapper mapper;
    private final StudentProfileConverter converter;

    @Override
    public Optional<StudentProfile> findByUserIdAndCampusId(Long userId, Long campusId) {
        StudentProfilePo po = mapper.selectOne(
            new LambdaQueryWrapper<StudentProfilePo>()
                .eq(StudentProfilePo::getUserId, userId)
                .eq(StudentProfilePo::getCampusId, campusId)
                .eq(StudentProfilePo::getDeleted, 0)
        );
        return Optional.ofNullable(po).map(converter::toDomain);
    }

    @Override
    public Optional<StudentProfile> findByStudentNo(String studentNo) {
        StudentProfilePo po = mapper.selectOne(
            new LambdaQueryWrapper<StudentProfilePo>()
                .eq(StudentProfilePo::getStudentNo, studentNo)
                .eq(StudentProfilePo::getDeleted, 0)
        );
        return Optional.ofNullable(po).map(converter::toDomain);
    }

    @Override
    public void save(StudentProfile student) {
        StudentProfilePo po = converter.toPo(student);
        if (po.getStudentId() == null) {
            mapper.insert(po);
            student.setStudentId(po.getStudentId());
        } else {
            mapper.updateById(po);
        }
    }

}
