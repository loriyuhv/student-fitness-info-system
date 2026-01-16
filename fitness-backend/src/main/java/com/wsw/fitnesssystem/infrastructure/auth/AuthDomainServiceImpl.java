package com.wsw.fitnesssystem.infrastructure.auth;

import com.wsw.fitnesssystem.common.exception.BizException;
import com.wsw.fitnesssystem.domain.auth.model.AuthUser;
import com.wsw.fitnesssystem.domain.auth.service.AuthDomainService;
import com.wsw.fitnesssystem.infrastructure.persistence.entity.SysUser;
import com.wsw.fitnesssystem.infrastructure.persistence.mapper.SysPermissionMapper;
import com.wsw.fitnesssystem.infrastructure.persistence.mapper.SysUserMapper;
import com.wsw.fitnesssystem.interfaces.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/15 14:43
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthDomainServiceImpl implements AuthDomainService {
    private final SysUserMapper sysUserMapper;
    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public AuthUser loadByUsername(String username) {
        SysUser user = sysUserMapper.selectByUsername(username);

        if (user == null) {
            log.warn("登录失败：用户不存在 username={}", username);
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }
        if (user.getStatus() == 0) {
            log.warn("登录失败：用户已被禁用 username={}", username);
            throw new BizException(ResultCode.ACCOUNT_DISABLED);
        }

        Set<String> perms =
            sysPermissionMapper.selectPermCodesByUserId(user.getUserId());

        return new AuthUser(
            user.getUserId(),
            user.getUsername(),
            user.getPassword(),
            user.getStatus(),
            perms
        );
    }
}
