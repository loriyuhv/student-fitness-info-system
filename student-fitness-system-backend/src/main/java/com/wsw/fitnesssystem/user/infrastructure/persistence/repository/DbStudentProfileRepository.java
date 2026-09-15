package com.wsw.fitnesssystem.user.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wsw.fitnesssystem.shared.response.PageResult;
import com.wsw.fitnesssystem.user.domain.model.StudentProfile;
import com.wsw.fitnesssystem.user.domain.repository.StudentProfileRepository;
import com.wsw.fitnesssystem.user.infrastructure.persistence.converter.StudentProfileConverter;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.StudentProfilePo;
import com.wsw.fitnesssystem.user.infrastructure.persistence.mapper.StudentProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 09:03
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class DbStudentProfileRepository implements StudentProfileRepository {

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

    @Override
    public PageResult<StudentProfile> page(int pageNum, int pageSize) {
        Page<StudentProfilePo> mpPage = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<StudentProfilePo> wrapper = new LambdaQueryWrapper<StudentProfilePo>()
            .eq(StudentProfilePo::getStatus, 1)    // 在籍
            .eq(StudentProfilePo::getDeleted, 0)
            .orderByDesc(StudentProfilePo::getStudentId);

        Page<StudentProfilePo> result = mapper.selectPage(mpPage, wrapper);

        List<StudentProfile> items = result.getRecords().stream()
            .map(converter::toDomain)
            .toList();

        return PageResult.of(items, result.getTotal(), pageNum, pageSize);
    }

}
