package com.wsw.fitnesssystem.user.application.port.output;

import com.wsw.fitnesssystem.user.application.dto.result.UserAccountResult;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/16 11:58
 * @since 1.0
 */
public interface UserAccountQueryPort {

    Optional<UserAccountResult> findByUserIdAndCampusId(Long userId, Long campusId);

    Optional<UserAccountResult> findByUsername(String username);

    Set<String> findExistingUsernames(Collection<String> usernames);

    /**
     * 按 userId 查询（不带 campusId）。
     * <p><b>数据范围：</b>由 {@code sys_user} 表的 COLLEGE 契约控制，
     * 校区管理员自动追加 {@code campus_id = 本校区}。</p>
     */
    Optional<UserAccountResult> findByUserId(Long userId);

}
