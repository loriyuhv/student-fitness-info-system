package com.wsw.fitnesssystem.auth.infrastructure.persistence.repository;

import com.wsw.fitnesssystem.auth.domain.model.User;
import com.wsw.fitnesssystem.auth.domain.port.UserRepository;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.entity.SysUser;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.mapper.SysUserMapper;
import com.wsw.fitnesssystem.shared.common.exception.SystemException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository 的 MyBatisPlus 实现
 * 典型的「基础设施 → 领域适配器」
 *
 * @author loriyuhv
 * @version 1.0 2026/1/19 14:43
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final SysUserMapper sysUserMapper;

    @Override
    public Optional<User> findByUsername(String username) {
        try {
            SysUser sysUser = sysUserMapper.selectByUsername(username);
            return Optional.ofNullable(toDomain(sysUser));
        } catch (Exception e) {
            // 基础设施异常 → SystemException
            throw new SystemException(ResultCode.SYSTEM_ERROR, e);
        }
    }

    /**
     * Entity → Domain 转换
     */
    private User toDomain(SysUser entity) {
        if (entity == null) {
            return null;
        }
        return new User(
            entity.getUserId(),
            entity.getUsername(),
            entity.getPassword(),
            entity.getStatus()
        );
    }
}
