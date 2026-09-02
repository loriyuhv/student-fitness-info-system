package com.wsw.fitnesssystem.user.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsw.fitnesssystem.user.domain.model.UserProfile;
import com.wsw.fitnesssystem.user.domain.port.UserProfileRepository;
import com.wsw.fitnesssystem.user.infrastructure.persistence.converter.UserProfileConverter;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.UserProfilePo;
import com.wsw.fitnesssystem.user.infrastructure.persistence.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 09:02
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class UserProfileRepositoryImpl implements UserProfileRepository {

    private final UserProfileMapper mapper;
    private final UserProfileConverter converter;

    @Override
    public Optional<UserProfile> findByUserIdAndCampusId(Long userId, Long campusId) {
        UserProfilePo po = mapper.selectOne(
            new LambdaQueryWrapper<UserProfilePo>()
                .eq(UserProfilePo::getUserId, userId)
                .eq(UserProfilePo::getCampusId, campusId)
                .eq(UserProfilePo::getDeleted, 0)
        );
        return Optional.ofNullable(po).map(converter::toDomain);
    }

    @Override
    public void save(UserProfile profile) {
        UserProfilePo po = converter.toPo(profile);
        if (po.getProfileId() == null) {
            mapper.insert(po);
            profile.setProfileId(po.getProfileId());
        } else {
            mapper.updateById(po);
        }
    }

}
