package com.wsw.fitnesssystem.auth.audit.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsw.fitnesssystem.auth.audit.domain.model.LoginAudit;
import com.wsw.fitnesssystem.auth.audit.domain.port.LoginAuditRepository;
import com.wsw.fitnesssystem.auth.audit.infrastructure.persistence.db.converter.LoginAuditConverter;
import com.wsw.fitnesssystem.auth.audit.infrastructure.persistence.db.entity.SysUserLogin;
import com.wsw.fitnesssystem.auth.audit.infrastructure.persistence.db.mapper.SysUserLoginMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 登录审计仓储实现 (MySQL)
 *
 * @author loriyuhv
 * @version 1.0 2026/8/25 13:36
 * @since 1.0
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class LoginAuditRepositoryImpl implements LoginAuditRepository {

    private final SysUserLoginMapper sysUserLoginMapper;

    @Override
    public void save(LoginAudit audit) {
        SysUserLogin entity = LoginAuditConverter.toEntity(audit);
        sysUserLoginMapper.insert(entity);
    }

    @Override
    public void update(LoginAudit audit) {
        SysUserLogin entity = LoginAuditConverter.toEntity(audit);
        // 通过 tokenId 定位记录更新
        sysUserLoginMapper.update(entity, new LambdaQueryWrapper<SysUserLogin>()
            .eq(SysUserLogin::getTokenId, entity.getTokenId())
            .eq(SysUserLogin::getStatus, 1)
        );
    }

    @Override
    public Optional<LoginAudit> findByTokenId(String tokenId) {
        SysUserLogin entity = sysUserLoginMapper.selectOne(new LambdaQueryWrapper<SysUserLogin>()
            .eq(SysUserLogin::getTokenId, tokenId)
            .eq(SysUserLogin::getStatus, 1)
            .last("LIMIT 1")
        );
        return Optional.ofNullable(entity).map(LoginAuditConverter::toDomain);
    }

}
