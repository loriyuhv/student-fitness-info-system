package com.wsw.fitnesssystem.user.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsw.fitnesssystem.user.domain.model.StudentProfile;
import com.wsw.fitnesssystem.user.domain.repository.StudentProfileRepository;
import com.wsw.fitnesssystem.user.infrastructure.persistence.converter.StudentProfileConverter;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.StudentProfilePo;
import com.wsw.fitnesssystem.user.infrastructure.persistence.mapper.StudentProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
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
        );
        return Optional.ofNullable(po).map(converter::toDomain);
    }

    @Override
    public Optional<StudentProfile> findByStudentNo(String studentNo) {
        StudentProfilePo po = mapper.selectOne(
            new LambdaQueryWrapper<StudentProfilePo>()
                .eq(StudentProfilePo::getStudentNo, studentNo)
        );
        return Optional.ofNullable(po).map(converter::toDomain);
    }

    @Override
    public Optional<StudentProfile> findByUserId(Long userId) {
        LambdaQueryWrapper<StudentProfilePo> wrapper = new LambdaQueryWrapper<StudentProfilePo>()
            .eq(StudentProfilePo::getUserId, userId);
        // ⚠️ 不显式加 campus_id 条件，由数据权限拦截器追加
        return Optional.ofNullable(mapper.selectOne(wrapper))
            .map(converter::toDomain);
    }

    @Override
    public List<StudentProfile> findByUserIds(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) return Collections.emptyList();

        LambdaQueryWrapper<StudentProfilePo> wrapper = new LambdaQueryWrapper<StudentProfilePo>()
            .in(StudentProfilePo::getUserId, userIds);

        List<StudentProfilePo> poList = mapper.selectList(wrapper);

        if (CollectionUtils.isEmpty(poList)) return Collections.emptyList();

        return poList.stream().map(converter::toDomain).toList();
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
