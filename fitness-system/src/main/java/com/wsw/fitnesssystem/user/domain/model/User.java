package com.wsw.fitnesssystem.user.domain.model;

import com.wsw.fitnesssystem.user.domain.valueobject.UserSource;
import com.wsw.fitnesssystem.user.domain.valueobject.Status;
import com.wsw.fitnesssystem.user.domain.valueobject.UserType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
@Setter
@Getter
@Builder
public class User {

    // ==================== 核心标识 ====================

    /** 用户ID（数据库自增） */
    private Long userId;

    /** 校区ID（多租户隔离） */
    private Long campusId;

    // ==================== 认证信息 ====================

    /** 登录账号（唯一） */
    private String username;

    /** BCrypt 哈希值（仅持有，不处理逻辑）*/
    private String password;

    // ==================== 个人资料 ====================

    /** 昵称（显示名称） */
    private String nickname;

    /** 手机号码 */
    private String phoneNumber;

    /** 邮箱 */
    private String email;

    /** 备注 */
    private String remark;

    // ==================== 角色与状态 ====================

    /** 用户类型：0-管理员，1-教师，2-学生 */
    private UserType userType;

    /** 用户来源：0-导入，1-同步，2-手动 */
    private UserSource source;

    /** 启用状态：0-禁用，1-启用 */
    private Status status;

    /** 逻辑删除标记 */
    private boolean deleted;

    // ==================== 审计字段 ====================

    private Long createBy;
    private Long updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ==================== 业务方法 ====================

    /**
     * 启用用户（业务规则：只有禁用的才能启用）
     */
    public void enable() {
        if (this.status == Status.ENABLED) {
            throw new IllegalStateException("用户已是启用状态");
        }
        this.status = Status.ENABLED;
    }

    /**
     * 禁用用户（管理员不允许被禁用）
     */
    public void disable() {
        if (this.userType == UserType.ADMIN) {
            throw new IllegalStateException("不允许禁用管理员账号");
        }
        this.status = Status.DISABLED;
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

    // ==================== 辅助方法 ====================

    /**
     * 判断用户是否启用
     */
    public boolean isEnabled() {
        return status == Status.ENABLED;
    }

    /**
     * 判断用户是否禁用
     */
    public boolean isDisabled() {
        return status == Status.DISABLED;
    }

    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    /**
     * 判断是否为教师
     */
    public boolean isTeacher() {
        return userType == UserType.TEACHER;
    }

    /**
     * 判断是否为学生
     */
    public boolean isStudent() {
        return userType == UserType.STUDENT;
    }

}
