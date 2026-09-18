package com.wsw.fitnesssystem.iam.authentication.domain.repository;

import com.wsw.fitnesssystem.iam.authentication.domain.model.AuthAccount;
import com.wsw.fitnesssystem.iam.authentication.domain.query.AuthAccountQuery;
import com.wsw.fitnesssystem.shared.domain.pagination.PageSlice;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * 认证账号仓储。
 *
 * <p><b>依赖边界：</b>只使用 iam 领域模型（{@link AuthAccount}）与
 * 通用领域原语（{@link PageSlice}），不引用任何其他模块的应用层 DTO
 * 或响应结构。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/16 11:56
 * @since 1.0
 */
public interface AuthAccountRepository {

    AuthAccount save(AuthAccount account);

    Optional<AuthAccount> findByUserIdAndCampusId(Long userId, Long campusId);

    Optional<AuthAccount> findByUsername(String username);

    /**
     * 按 userId 查询（不带 campusId）。
     * <p><b>数据范围：</b>由 {@code sys_user} 表的 COLLEGE 契约控制，
     * 校区管理员自动追加 {@code campus_id = 本校区}。</p>
     */
    Optional<AuthAccount> findByUserId(Long userId);

    Set<String> findExistingUsernames(Collection<String> usernames);

    boolean existsByUsername(String username);

    /**
     * 分页查询账号（读模型）。
     *
     * <p><b>数据范围：</b>由 {@code sys_user} 表的 COLLEGE 契约控制，
     * 数据权限拦截器会自动追加 {@code campus_id = 本校区}（针对校区管理员）。</p>
     *
     * @param query iam 领域层查询条件
     * @return 领域分页切片，包含总数与当前页数据
     */
    PageSlice<AuthAccount> page(AuthAccountQuery query);

}
