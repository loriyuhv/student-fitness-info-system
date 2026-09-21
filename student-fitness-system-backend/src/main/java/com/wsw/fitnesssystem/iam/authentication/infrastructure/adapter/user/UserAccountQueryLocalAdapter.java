package com.wsw.fitnesssystem.iam.authentication.infrastructure.adapter.user;

import com.wsw.fitnesssystem.iam.authentication.domain.model.AuthAccount;
import com.wsw.fitnesssystem.iam.authentication.domain.query.AuthAccountQuery;
import com.wsw.fitnesssystem.iam.authentication.domain.repository.AuthAccountRepository;
import com.wsw.fitnesssystem.shared.domain.pagination.PageSlice;
import com.wsw.fitnesssystem.user.application.dto.query.UserListQuery;
import com.wsw.fitnesssystem.user.application.dto.result.UserAccountResult;
import com.wsw.fitnesssystem.user.application.port.output.UserAccountQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

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


    @Override
    public PageSlice<UserAccountResult> page(UserListQuery query) {
        // 1. 入向翻译：user DTO → iam 领域查询对象
        AuthAccountQuery iamQuery = new AuthAccountQuery(
            query.pageNum(), query.pageSize(), query.userType(), query.status(), query.keyword()
        );

        // 2. 调用 iam 领域仓储
        PageSlice<AuthAccount> slice = repository.page(iamQuery);

        // 3. 出项翻译
        return PageSlice.of(
            slice.items().stream().map(this::buildResult).toList(),
            slice.total(), slice.pageNum(), slice.pageSize()
        );
    }

    // ==================== 出向翻译：iam → user ====================

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
