package com.wsw.fitnesssystem.user.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsw.fitnesssystem.user.domain.model.UserProfile;
import com.wsw.fitnesssystem.user.domain.repository.UserProfileRepository;
import com.wsw.fitnesssystem.user.infrastructure.persistence.converter.UserProfileConverter;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.UserProfilePo;
import com.wsw.fitnesssystem.user.infrastructure.persistence.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 09:02
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class DbUserProfileRepository implements UserProfileRepository {

    private final UserProfileMapper mapper;

    @Override
    public Optional<UserProfile> findByUserIdAndCampusId(Long userId, Long campusId) {
        LambdaQueryWrapper<UserProfilePo> wrapper = new LambdaQueryWrapper<UserProfilePo>()
            .eq(UserProfilePo::getUserId, userId)
            .eq(UserProfilePo::getCampusId, campusId);
        return Optional.ofNullable(mapper.selectOne(wrapper)).map(UserProfileConverter::toDomain);
    }

    @Override
    public List<UserProfile> findByUserIds(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) return Collections.emptyList();

        LambdaQueryWrapper<UserProfilePo> wrapper = new LambdaQueryWrapper<UserProfilePo>()
            .in(UserProfilePo::getUserId, userIds);

        List<UserProfilePo> poList = mapper.selectList(wrapper);

        return UserProfileConverter.toDomainList(poList);
    }

    @Override
    public void save(UserProfile profile) {
        UserProfilePo po = UserProfileConverter.toPo(profile);
        if (po.getProfileId() == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
    }

}
