package com.wsw.fitnesssystem.iam.authentication.domain.model;

import com.wsw.fitnesssystem.iam.authentication.domain.vb.AccountStatus;
import com.wsw.fitnesssystem.iam.authentication.domain.vb.UserSource;
import com.wsw.fitnesssystem.iam.authentication.domain.vb.UserType;
import lombok.Getter;

/**
 * 认证用户账号聚合根（Auth 模块核心领域模型）
 *
 * @author loriyuhv
 * @version 1.0 2026/9/16 11:44
 * @since 1.0
 */
@Getter
public class AuthAccount {

    private final Long userId;
    private final Long campusId;
    private final String username;
    private String passwordHash;
    private final UserType userType;
    private final UserSource source;
    private AccountStatus status;
    private Integer deleted;
    private final Long createBy;
    private Long updateBy;

    // ==================== 工厂方法 ====================

    /** 创建新账号（注册 / 导入场景） */
    public static AuthAccount create(
        Long campusId, String username, String passwordHash,
        UserType userType, UserSource source, Long operatorId
    ) {
        if (campusId == null) {
            throw new IllegalArgumentException("campusId 不能为空");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username 不能为空");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash 不能为空");
        }
        if (userType == null) {
            throw new IllegalArgumentException("userType 不能为空");
        }
        if (source == null) {
            throw new IllegalArgumentException("source 不能为空");
        }
        return new AuthAccount(
            null, campusId, username, passwordHash, userType, source,
            AccountStatus.ENABLED, 0, operatorId, operatorId
        );
    }

    /** 从持久化重建（仅 Repository 使用） */
    public static AuthAccount reconstitute(
        Long userId, Long campusId, String username, String passwordHash, UserType userType,
        UserSource source, AccountStatus status, Integer deleted, Long createBy, Long updateBy
    ) {
        return new AuthAccount(
            userId, campusId, username, passwordHash, userType, source, status, deleted, createBy, updateBy
        );
    }

    private AuthAccount(
        Long userId, Long campusId, String username, String passwordHash, UserType userType,
        UserSource source, AccountStatus status, Integer deleted, Long createBy, Long updateBy
    ) {
        this.userId = userId;
        this.campusId = campusId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.userType = userType;
        this.source = source;
        this.status = status;
        this.deleted = deleted;
        this.createBy = createBy;
        this.updateBy = updateBy;
    }

    // ==================== 领域行为 ====================

    public void enable(Long operatorId) {
        if (this.status == AccountStatus.ENABLED) {
            throw new IllegalStateException("账号已处于启用状态");
        }
        this.status = AccountStatus.ENABLED;
        this.updateBy = operatorId;
    }

    public void disable(Long operatorId) {
        if (this.status == AccountStatus.DISABLED) {
            throw new IllegalStateException("账号已处于禁用状态");
        }
        this.status = AccountStatus.DISABLED;
        this.updateBy = operatorId;
    }

    public void changePassword(String newPasswordHash, Long operatorId) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash 不能为空");
        }
        this.passwordHash = newPasswordHash;
        this.updateBy = operatorId;
    }

    public void softDelete(Long operatorId) {
        if (isDeleted()) {
            throw new IllegalStateException("账号已删除");
        }
        this.deleted = 1;
        this.updateBy = operatorId;
    }

    public void restore(Long operatorId) {
        if (!isDeleted()) {
            throw new IllegalStateException("账号未被删除，无法恢复");
        }
        this.deleted = 0;
        this.updateBy = operatorId;
    }

    // ==================== 查询语义 ====================

    public boolean isEnabled() {
        return this.status == AccountStatus.ENABLED && !isDeleted();
    }

    public boolean isDeleted() {
        return this.deleted != null && this.deleted == 1;
    }

    public boolean canLogin() {
        return isEnabled();
    }

}
