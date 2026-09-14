package com.wsw.fitnesssystem.user.infrastructure.adapter;

import com.wsw.fitnesssystem.iam.authentication.application.dto.result.UserCredentialResult;
import com.wsw.fitnesssystem.iam.authentication.application.port.output.UserCredentialQueryPort;
import com.wsw.fitnesssystem.user.domain.model.User;
import com.wsw.fitnesssystem.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户凭证查询本地适配器。
 *
 * <p><b>职责：</b>实现 authentication 模块定义的 {@link UserCredentialQueryPort}，
 * 将 user 模块的 {@link User} 转换为 authentication 所需的 {@link UserCredentialResult}。</p>
 *
 * <p><b>依赖方向：</b>本类依赖 authentication 的 Port 和 DTO（契约），
 * authentication 不反向依赖 user。</p>
 *
 * <p><b>演进：</b>微服务阶段本类被拆成两部分——user 侧暴露 REST Controller，
 * authentication 侧新增 {@code UserCredentialQueryFeignAdapter}。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 15:13
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCredentialQueryLocalAdapter implements UserCredentialQueryPort {

    private final UserRepository userRepository;

    @Override
    public UserCredentialResult findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return userRepository.findByUsername(username)
            .map(this::buildResult)
            .orElse(null);
    }

    @Override
    public UserCredentialResult findByCampusIdAndUserId(Long campusId, Long userId) {
        if (campusId == null || userId == null) {
            return null;
        }
        return userRepository.findByCampusIdAndUserId(campusId, userId)
            .map(this::buildResult)
            .orElse(null);
    }

    /**
     * 领域模型 → 认证凭证结果。
     * <p>只映射认证字段，绝不携带档案字段。</p>
     */
    private UserCredentialResult buildResult(User user) {
        return UserCredentialResult.builder()
            .userId(user.getUserId())
            .campusId(user.getCampusId())
            .username(user.getUsername())
            .passwordHash(user.getPassword())
            .userType(user.getUserType().getCode())
            .status(user.getStatus().getCode())
            .build();
    }

}
