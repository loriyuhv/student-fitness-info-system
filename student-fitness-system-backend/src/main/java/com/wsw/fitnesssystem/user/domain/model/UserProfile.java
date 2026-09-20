package com.wsw.fitnesssystem.user.domain.model;

import com.wsw.fitnesssystem.shared.domain.exception.DomainConflictException;
import com.wsw.fitnesssystem.shared.domain.exception.DomainValidationException;
import com.wsw.fitnesssystem.shared.kernel.exception.BizException;
import com.wsw.fitnesssystem.shared.kernel.error.CommonErrorCode;
import com.wsw.fitnesssystem.user.domain.vb.Gender;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户档案聚合根
 * <p>承载 sys_user 的垂直扩展：昵称、联系方式、画像字段。
 *
 * <p><b>不可变标识：</b>profileId / userId / campusId / createBy / createTime
 * <p><b>可变画像：</b>nickname / phoneNumber / email / gender / birthDate / avatarUrl / address / remark
 *
 * @author loriyuhv
 * @version 1.0 2026/9/2 08:53
 * @since 1.0
 */
@Getter
public class UserProfile {

    // ==================== 不可变标识 ====================

    private final Long profileId;
    private final Long userId;
    private final Long campusId;

    // ==================== 可变画像 ====================

    private String nickname;
    private String phoneNumber;
    private String email;
    private Gender gender;
    private LocalDate birthDate;
    private String avatarUrl;
    private String address;
    private String remark;

    // ==================== 状态 ====================
    private boolean deleted;

    // ==================== 审计 ====================
    private final Long createBy;
    private final LocalDateTime createTime;
    private Long updateBy;
    private final LocalDateTime updateTime;

    // ==================== 工厂方法 ====================

    /**
     * 创建新档案（注册 / 导入场景）。
     *
     * <p>nickname 必填；其余画像字段由后续 {@code updateXxx} 补充。
     */
    public static UserProfile create(
        Long userId, Long campusId, String nickname, Long operatorId
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        if (campusId == null) {
            throw new IllegalArgumentException("campusId 不能为空");
        }
        if (StringUtils.isBlank(nickname)) {
            throw new DomainValidationException("昵称不能为空");
        }
        if (operatorId == null) {
            throw new IllegalArgumentException("operatorId 不能为空");
        }
        return new UserProfile(
            null, userId, campusId, nickname.trim(), null,
            null, null, null, null, null,
            null, false, operatorId, null, operatorId, null
        );
    }

    /** 从持久化重建（仅 Repository 使用） */
    public static UserProfile reconstitute(
        Long profileId, Long userId, Long campusId, String nickname, String phoneNumber,
        String email, Gender gender, LocalDate birthDate, String avatarUrl, String address, String remark,
        boolean deleted, Long createBy, LocalDateTime createTime, Long updateBy, LocalDateTime updateTime
    ) {
        return new UserProfile(
            profileId, userId, campusId, nickname, phoneNumber, email, gender, birthDate,
            avatarUrl, address, remark, deleted, createBy, createTime, updateBy, updateTime
        );
    }

    private UserProfile(
        Long profileId, Long userId, Long campusId, String nickname, String phoneNumber,
        String email, Gender gender, LocalDate birthDate, String avatarUrl, String address, String remark,
        boolean deleted, Long createBy, LocalDateTime createTime, Long updateBy, LocalDateTime updateTime
    ) {
        this.profileId = profileId;
        this.userId = userId;
        this.campusId = campusId;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.gender = gender;
        this.birthDate = birthDate;
        this.avatarUrl = avatarUrl;
        this.address = address;
        this.remark = remark;
        this.deleted = deleted;
        this.createBy = createBy;
        this.createTime = createTime;
        this.updateBy = updateBy;
        this.updateTime = updateTime;
    }

    // ==================== 领域行为 ====================

    /** 更新昵称（非空） */
    public void updateNickname(String nickname, Long operatorId) {
        if (StringUtils.isBlank(nickname)) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "昵称不能为空");
        }
        this.nickname = nickname.trim();
        this.updateBy = operatorId;
    }

    /** 更新手机号（允许 null / 空串表示清空） */
    public void updatePhoneNumber(String phoneNumber, Long operatorId) {
        if (StringUtils.isBlank(phoneNumber)) {
            String trimmed = phoneNumber.trim();
            if (!trimmed.matches("^1[3-9]\\d{9}$")) {
                throw new DomainValidationException("手机号格式不正确");
            }
            this.phoneNumber = trimmed;
        } else {
            this.phoneNumber = null;
        }
        this.updateBy = operatorId;
    }

    /** 更新邮箱（允许 null / 空串表示清空） */
    public void updateEmail(String email, Long operatorId) {
        if (StringUtils.isBlank(email)) {
            String trimmed = email.trim();
            if (!trimmed.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$")) {
                throw new DomainValidationException("邮箱格式不正确");
            }
            this.email = trimmed;
        } else {
            this.email = null;
        }
        this.updateBy = operatorId;
    }

    /** 更新性别 */
    public void updateGender(Gender gender, Long operatorId) {
        this.gender = gender;
        this.updateBy = operatorId;
    }

    /** 更新出生日期（不能晚于今天） */
    public void updateBirthDate(LocalDate birthDate, Long operatorId) {
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new DomainValidationException("出生日期不能晚于今天");
        }
        this.birthDate = birthDate;
        this.updateBy = operatorId;
    }

    /** 更新头像 URL */
    public void updateAvatarUrl(String avatarUrl, Long operatorId) {
        this.avatarUrl = avatarUrl;
        this.updateBy = operatorId;
    }

    /** 更新联系地址 */
    public void updateAddress(String address, Long operatorId) {
        this.address = address;
        this.updateBy = operatorId;
    }

    /** 更新备注 */
    public void updateRemark(String remark, Long operatorId) {
        this.remark = remark;
        this.updateBy = operatorId;
    }

    /** 逻辑删除 */
    public void softDelete(Long operatorId) {
        if (this.deleted) {
            throw new DomainConflictException("档案已删除，无法重复操作");
        }
        this.deleted = true;
        this.updateBy = operatorId;
    }

}
