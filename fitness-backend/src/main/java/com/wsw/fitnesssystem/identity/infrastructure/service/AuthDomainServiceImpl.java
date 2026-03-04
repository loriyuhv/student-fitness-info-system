package com.wsw.fitnesssystem.identity.infrastructure.service;

import com.wsw.fitnesssystem.shared.common.exception.BizException;
import com.wsw.fitnesssystem.identity.domain.model.AuthUser;
import com.wsw.fitnesssystem.identity.domain.repository.AuthUserRepository;
import com.wsw.fitnesssystem.identity.domain.service.AuthDomainService;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


/**
 * 真正校验用户信息的地方
 *
 * @author loriyuhv
 * @version 1.0 2026/1/15 14:43
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthDomainServiceImpl implements AuthDomainService {
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthUser login(String username, String rawPassword) {
        // 1. 查用户
        AuthUser user = authUserRepository.findByUsername(username)
            .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));

        // 2. 校验状态
        if (!user.isEnabled()) {
            throw new BizException(ResultCode.ACCOUNT_DISABLED);
        }

        // 3. 校验密码（核心）
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BizException(ResultCode.PASSWORD_ERROR);
        }

        // 4. 认证成功
        return user;
    }
}
