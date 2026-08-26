package com.wsw.fitnesssystem.user.domain.model;

import com.wsw.fitnesssystem.user.domain.valueobject.UserSource;
import com.wsw.fitnesssystem.user.domain.valueobject.UserStatus;
import com.wsw.fitnesssystem.user.domain.valueobject.UserType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 用户聚合根（User 模块核心领域模型）
 *
 * <p><b>职责：</b></p>
 * <ul>
 *     <li>代表系统用户（包含认证密码哈希 + 个人资料）</li>
 *     <li>封装用户资料更新、状态切换、逻辑删除等业务规则</li>
 *     <li><b>注意：</b>密码（password）在此仅作为“不透明数据”持有，
 *     密码校验和加密逻辑由 Auth 模块的 {@code AuthUser} 负责</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 12:36
 * @since 1.0
 */
@Getter
@Builder
public class User {

    private Long userId;
    private Long campusId;
    private String username;
    /** BCrypt 哈希值（仅持有，不处理逻辑）*/
    private String password;
    private String nickname;
    private String phoneNumber;
    private String email;
    private String remark;
    private UserType userType;
    private UserSource source;
    /** 启用/禁用 */
    private UserStatus status;
    /** 逻辑删除标记 */
    private boolean deleted;
    private Long createBy;
    private Long updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 启用用户（业务规则：只有禁用的才能启用）
     */
    public void enable() {
        if (this.status == UserStatus.ENABLED) {
            throw new IllegalStateException("用户已是启用状态");
        }
        this.status = UserStatus.ENABLED;
    }

    /**
     * 禁用用户（业务规则：不能禁用管理员？可自定义）
     */
    public void disable() {
        if (this.userType == UserType.ADMIN) {
            throw new IllegalStateException("不允许禁用管理员账号");
        }
        this.status = UserStatus.DISABLED;
    }

    /**
     * 更新手机号（业务规则：校验手机号格式）
     */
    public void updatePhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        this.phoneNumber = phoneNumber;
    }

    /**
     * 更新昵称
     */
    public void updateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("昵称不能为空");
        }
        this.nickname = nickname;
    }

    /**
     * 逻辑删除（软删除）
     */
    public void softDelete() {
        if (this.deleted) {
            throw new IllegalStateException("用户已被删除");
        }
        this.deleted = true;
    }

    /**
     * 恢复逻辑删除
     */
    public void restore() {
        if (!this.deleted) {
            throw new IllegalStateException("用户未删除，无需恢复");
        }
        this.deleted = false;
    }

}
