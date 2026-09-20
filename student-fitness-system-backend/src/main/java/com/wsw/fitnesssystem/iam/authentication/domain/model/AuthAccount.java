package com.wsw.fitnesssystem.iam.authentication.domain.model;

import com.wsw.fitnesssystem.iam.authentication.domain.port.PasswordEncryptor;
import com.wsw.fitnesssystem.iam.authentication.domain.vb.AccountStatus;
import com.wsw.fitnesssystem.iam.authentication.domain.vb.UserSource;
import com.wsw.fitnesssystem.iam.authentication.domain.vb.UserType;
import com.wsw.fitnesssystem.iam.error.IamAuthNErrorCode;
import com.wsw.fitnesssystem.iam.error.IamRiskErrorCode;
import com.wsw.fitnesssystem.shared.application.exception.BizException;
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
    private boolean deleted;
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
            AccountStatus.ENABLED, false, operatorId, operatorId
        );
    }

    /** 从持久化重建（仅 Repository 使用） */
    public static AuthAccount reconstitute(
        Long userId, Long campusId, String username, String passwordHash, UserType userType,
        UserSource source, AccountStatus status, boolean deleted, Long createBy, Long updateBy
    ) {
        return new AuthAccount(
            userId, campusId, username, passwordHash, userType, source, status, deleted, createBy, updateBy
        );
    }

    private AuthAccount(
        Long userId, Long campusId, String username, String passwordHash, UserType userType,
        UserSource source, AccountStatus status, boolean deleted, Long createBy, Long updateBy
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
        this.deleted = true;
        this.updateBy = operatorId;
    }

    /**
     * 恢复已删除账号
     * <p><b>⚠️ 实现约束：</b>本方法把 deleted 从 1 改回 0。若通过
     * {@code DbAuthAccountRepository.save()} 持久化，会因 {@code @TableLogic}
     * 自动追加的 {@code AND deleted = 0} 条件导致更新失败。
     * 恢复功能必须走自定义 SQL（{@code SysUserMapper.restoreById}）。
     *
     * @param operatorId 操作者ID
     */
    public void restore(Long operatorId) {
        if (!isDeleted()) {
            throw new IllegalStateException("账号未被删除，无法恢复");
        }
        this.deleted = false;
        this.updateBy = operatorId;
    }

    // ==================== 认证行为 ====================

    /**
     * 校验密码（登录场景）
     *
     * <p>流程：先检查账号可用状态，再比对 BCrypt 密文。
     * <p>失败时抛出 {@link BizException}，由应用层统一处理。
     */
    public void verifyPassword(String rawPassword, PasswordEncryptor encryptor) {
        if (!canLogin()) {
            throw new BizException(IamRiskErrorCode.ACCOUNT_DISABLED);
        }
        boolean matches = encryptor.matches(rawPassword, this.passwordHash);
        if (!matches) {
            throw new BizException(IamAuthNErrorCode.PASSWORD_ERROR);
        }
    }

    // ==================== 查询语义 ====================

    public boolean isEnabled() {
        return this.status == AccountStatus.ENABLED && !isDeleted();
    }

    public boolean canLogin() {
        return isEnabled();
    }

}
