package com.wsw.fitnesssystem.user.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsw.fitnesssystem.user.domain.model.TeacherProfile;
import com.wsw.fitnesssystem.user.domain.repository.TeacherProfileRepository;
import com.wsw.fitnesssystem.user.infrastructure.persistence.converter.TeacherProfileConverter;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.TeacherProfilePo;
import com.wsw.fitnesssystem.user.infrastructure.persistence.mapper.TeacherProfileMapper;
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
public class DbTeacherProfileRepository implements TeacherProfileRepository {

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
    public Optional<TeacherProfile> findByUserId(Long userId) {
        LambdaQueryWrapper<TeacherProfilePo> wrapper = new LambdaQueryWrapper<TeacherProfilePo>()
            .eq(TeacherProfilePo::getUserId, userId);
        // ⚠️ 不显式加 campus_id 条件，由数据权限拦截器追加
        return Optional.ofNullable(mapper.selectOne(wrapper))
            .map(converter::toDomain);
    }

    @Override
    public List<TeacherProfile> findByUserIds(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) return Collections.emptyList();

        LambdaQueryWrapper<TeacherProfilePo> wrapper = new LambdaQueryWrapper<TeacherProfilePo>()
            .in(TeacherProfilePo::getUserId, userIds);

        List<TeacherProfilePo> poList = mapper.selectList(wrapper);

        if (CollectionUtils.isEmpty(poList)) return Collections.emptyList();

        return poList.stream().map(converter::toDomain).toList();
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
