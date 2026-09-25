package com.wsw.fitnesssystem.iam.authentication.domain.model;

import com.wsw.fitnesssystem.iam.authentication.domain.exception.DomainAuthNException;
import com.wsw.fitnesssystem.iam.authentication.domain.port.PasswordEncryptorPort;
import com.wsw.fitnesssystem.iam.authentication.domain.enums.AccountStatus;
import com.wsw.fitnesssystem.iam.authentication.domain.enums.UserSource;
import com.wsw.fitnesssystem.iam.authentication.domain.enums.UserType;
import com.wsw.fitnesssystem.shared.domain.exception.DomainConflictException;
import com.wsw.fitnesssystem.shared.domain.exception.DomainStateException;
import com.wsw.fitnesssystem.shared.domain.exception.DomainValidationException;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 认证用户账号聚合根（iam.authentication 核心领域模型）。
 *
 * <p><b>职责：</b>维护认证账号的状态与业务规则，包括账号启用/禁用、
 * 密码校验、软删除/恢复等。</p>
 *
 * <p><b>异常约定（分层规范）：</b>本类作为<b>领域模型</b>，
 * 只抛出 {@link com.wsw.fitnesssystem.shared.domain.exception.DomainException}
 * 的子类，不依赖任何 {@code ErrorCode}：</p>
 * <ul>
 *   <li>{@link DomainValidationException}：输入校验失败（必填为空、格式错误）</li>
 *   <li>{@link DomainConflictException}：业务规则冲突（如账号已删除）</li>
 *   <li>{@link DomainStateException}：状态迁移非法（如重复启用/禁用）</li>
 *   <li>{@link DomainAuthNException}：认证失败（密码不匹配）</li>
 * </ul>
 *
 * <p><b>异常翻译：</b>由应用层（如 {@code LoginCommandService}）捕获领域异常，
 * 翻译为携带 {@code ErrorCode} 的 {@code BizException}。</p>
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
            throw new DomainValidationException("campusId 不能为空");
        }
        if (StringUtils.isBlank(username)) {
            throw new DomainValidationException("username 不能为空");
        }
        if (StringUtils.isBlank(passwordHash)) {
            throw new DomainValidationException("passwordHash 不能为空");
        }
        if (userType == null) {
            throw new DomainValidationException("userType 不能为空");
        }
        if (source == null) {
            throw new DomainValidationException("source 不能为空");
        }
        return new AuthAccount(
            null, campusId, username, passwordHash, userType,
            source, AccountStatus.ENABLED, false, operatorId, operatorId
        );
    }

    /** 从持久化重建（仅 Repository 使用） */
    public static AuthAccount reconstitute(
        Long userId, Long campusId, String username, String passwordHash, UserType userType,
        UserSource source, AccountStatus status, boolean deleted, Long createBy, Long updateBy
    ) {
        return new AuthAccount(
            userId, campusId, username, passwordHash,
            userType, source, status, deleted, createBy, updateBy
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

    /**
     * 启用账号。
     *
     * @param operatorId 操作者 ID
     * @throws DomainStateException 账号已处于启用状态
     */
    public void enable(Long operatorId) {
        if (this.status == AccountStatus.ENABLED) {
            throw new DomainStateException("账号已处于启用状态");
        }
        this.status = AccountStatus.ENABLED;
        this.updateBy = operatorId;
    }

    /**
     * 禁用账号。
     *
     * @param operatorId 操作者 ID
     * @throws DomainStateException 账号已处于禁用状态
     */
    public void disable(Long operatorId) {
        if (this.status == AccountStatus.DISABLED) {
            throw new DomainStateException("账号已处于禁用状态");
        }
        this.status = AccountStatus.DISABLED;
        this.updateBy = operatorId;
    }

    /**
     * 修改密码哈希。
     *
     * @param newPasswordHash 新密码哈希（不可为空）
     * @param operatorId      操作者 ID
     * @throws DomainValidationException 密码哈希为空
     */
    public void changePassword(String newPasswordHash, Long operatorId) {
        if (StringUtils.isBlank(newPasswordHash)) {
            throw new DomainValidationException("密码哈希不能为空");
        }
        this.passwordHash = newPasswordHash;
        this.updateBy = operatorId;
    }


    /**
     * 软删除账号。
     *
     * @param operatorId 操作者 ID
     * @throws DomainConflictException 账号已被删除
     */
    public void softDelete(Long operatorId) {
        if (isDeleted()) {
            throw new DomainConflictException("账号已删除");
        }
        this.deleted = true;
        this.updateBy = operatorId;
    }

    /**
     * 恢复已删除账号。
     *
     * <p><b>⚠️ 实现约束：</b>本方法把 deleted 从 1 改回 0。若通过
     * {@code DbAuthAccountRepository.save()} 持久化，会因 {@code @TableLogic}
     * 自动追加的 {@code AND deleted = 0} 条件导致更新失败。
     * 恢复功能必须走自定义 SQL（{@code SysUserMapper.restoreById}）。</p>
     *
     * @param operatorId 操作者 ID
     * @throws DomainConflictException 账号未被删除
     */
    public void restore(Long operatorId) {
        if (!isDeleted()) {
            throw new DomainConflictException("账号未被删除，无法恢复");
        }
        this.deleted = false;
        this.updateBy = operatorId;
    }

    // ==================== 认证行为 ====================

    /**
     * 校验密码（登录场景）。
     *
     * <p><b>领域约束：</b>账号必须处于「可登录」状态（启用且未删除），
     * 且密码与存储的哈希匹配。</p>
     *
     * <p><b>异常语义：</b></p>
     * <ul>
     *   <li>{@link DomainStateException}：账号状态不允许登录（禁用或已删除）</li>
     *   <li>{@link DomainAuthNException}：密码不匹配</li>
     * </ul>
     *
     * <p><b>异常翻译：</b>由应用层 {@code AuthAppService} 捕获后，
     * 映射为业务错误码（如 {@code ACCOUNT_DISABLED} / {@code USER_LOGIN_ERROR}）。</p>
     *
     * @param rawPassword 明文密码
     * @param encryptor   密码加密器
     * @throws DomainStateException          账号状态不允许登录
     * @throws DomainAuthNException 密码不匹配
     */
    public void verifyPassword(String rawPassword, PasswordEncryptorPort encryptor) {
        if (!canLogin()) {
            throw new DomainStateException("账号状态不允许登录：" + status);
        }
        boolean matches = encryptor.matches(rawPassword, this.passwordHash);
        if (!matches) {
            throw new DomainAuthNException("密码不匹配");
        }
    }

    // ==================== 查询语义 ====================

    /**
     * 账号是否处于可用状态（启用且未删除）。
     */
    public boolean isEnabled() {
        return this.status == AccountStatus.ENABLED && !isDeleted();
    }

    /**
     * 账号是否可登录。
     */
    public boolean canLogin() {
        return isEnabled();
    }

}
