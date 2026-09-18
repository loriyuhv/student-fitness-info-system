package com.wsw.fitnesssystem.iam.authentication.infrastructure.adapter;

import com.wsw.fitnesssystem.iam.authentication.domain.model.AuthAccount;
import com.wsw.fitnesssystem.iam.authentication.domain.repository.AuthAccountRepository;
import com.wsw.fitnesssystem.user.application.dto.result.UserAccountResult;
import com.wsw.fitnesssystem.user.application.port.output.UserAccountQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * UserAccountQueryPort 本地实现
 *
 * <p>由 authentication 实现，供 user 模块查询认证账号信息。
 *
 * @author loriyuhv
 * @version 1.0 2026/9/16 15:49
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class UserAccountQueryLocalAdapter implements UserAccountQueryPort {

    private final AuthAccountRepository repository;

    @Override
    public Optional<UserAccountResult> findByUserIdAndCampusId(Long userId, Long campusId) {
        return repository.findByUserIdAndCampusId(userId, campusId).map(this::buildResult);
    }

    @Override
    public Optional<UserAccountResult> findByUsername(String username) {
        return repository.findByUsername(username).map(this::buildResult);
    }

    @Override
    public Optional<UserAccountResult> findByUserId(Long userId) {
        return repository.findByUserId(userId).map(this::buildResult);
    }

    @Override
    public Set<String> findExistingUsernames(Collection<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return Collections.emptySet();
        }
        return repository.findExistingUsernames(usernames);
    }

    private UserAccountResult buildResult(AuthAccount account) {
        return UserAccountResult.builder()
            .userId(account.getUserId())
            .campusId(account.getCampusId())
            .username(account.getUsername())
            .userType(account.getUserType().getCode())
            .source(account.getSource().getCode())
            .status(account.getStatus().getCode())
            .build();
    }

}
