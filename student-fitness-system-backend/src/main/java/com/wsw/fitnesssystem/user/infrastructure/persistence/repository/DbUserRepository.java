package com.wsw.fitnesssystem.user.infrastructure.persistence.repository;

import com.wsw.fitnesssystem.user.domain.model.User;
import com.wsw.fitnesssystem.user.domain.repository.UserRepository;
import com.wsw.fitnesssystem.user.infrastructure.persistence.converter.UserConverter;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.UserPo;
import com.wsw.fitnesssystem.user.infrastructure.persistence.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * 用户仓储实现（Infrastructure 层）
 * <p>实现 {@link UserRepository} 接口，使用 MyBatis-Plus 进行数据访问</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 13:58
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class DbUserRepository implements UserRepository {

    private final SysUserMapper userMapper;
    private final UserConverter userConverter;

    // ==================== 查询实现 ====================

    @Override
    public Optional<User> findByCampusIdAndUserId(Long campusId, Long userId) {
        UserPo po = userMapper.selectByCampusIdAndUserId(campusId, userId);
        return Optional.ofNullable(po).map(userConverter::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        UserPo po = userMapper.selectByUsername(username);
        return Optional.ofNullable(po).map(userConverter::toDomain);
    }

    @Override
    public Set<String> findExistingUsernames(List<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return new HashSet<>();
        }

        return new HashSet<>(userMapper.selectExistingUsernames(usernames));
    }

    @Override
    public List<User> findByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        List<UserPo> pos = userMapper.selectBatchIds(userIds);
        if (pos == null || pos.isEmpty()) {
            return List.of();
        }
        return pos.stream()
            .map(userConverter::toDomain)
            .toList();
    }

    // ==================== 写入实现 ====================

    @Override
    public void save(User user) {
        UserPo po = userConverter.toPo(user);
        userMapper.insert(po);
        // MyBatis-Plus 自动回填 userId 到 po，再回填到 user
        user.setUserId(po.getUserId());
    }

}
