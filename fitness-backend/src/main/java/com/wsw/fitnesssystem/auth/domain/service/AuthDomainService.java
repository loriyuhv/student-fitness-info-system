package com.wsw.fitnesssystem.auth.domain.service;

import com.wsw.fitnesssystem.auth.domain.model.AuthContext;
import com.wsw.fitnesssystem.auth.domain.model.User;
import com.wsw.fitnesssystem.auth.domain.port.PasswordEncryptor;
import com.wsw.fitnesssystem.auth.domain.port.UserRepository;
import com.wsw.fitnesssystem.shared.common.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 认证领域服务
 * 负责定义「系统如何认定一个用户是合法登录用户」
 * <p>注意：</p>
 * <ul>
 *     <li>这是业务规则，不是技术服务</li>
 *     <li>不设计成接口</li>
 * </ul>
 * @author loriyuhv
 * @version 1.0 2026/1/19 14:27
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class AuthDomainService {
    private final UserRepository userRepository;
    private final PasswordEncryptor passwordEncryptor;

    /**
     * 核心认证流程
     *
     * @param username     登录账号
     * @param rawPassword 明文密码
     * @return 认证上下文
     */
    public AuthContext authenticate(
        String username, String rawPassword
    ) {

        // 1.查询用户
        User user = userRepository.findByUsername(username)
            .orElseThrow(() ->
                new BizException(ResultCode.USER_NOT_EXIST)
            );

        // 2. 校验用户状态（领域规则）
        user.assertCanLogin();

        // 3. 校验密码（通过防腐层）
        boolean passwordMatch =
            passwordEncryptor.matches(rawPassword, user.password());

        if (!passwordMatch) {
            throw new BizException(ResultCode.PASSWORD_ERROR);
        }

        // 4. 返回认证结果
        return new AuthContext(user.userId());
    }
}
