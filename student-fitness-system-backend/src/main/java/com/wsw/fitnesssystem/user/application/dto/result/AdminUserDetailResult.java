package com.wsw.fitnesssystem.user.application.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * C2 结果：管理员视角的用户详情。
 *
 * <p><b>数据范围：</b>由数据权限拦截器在查询时收敛（COLLEGE / ALL）。</p>
 * <p><b>脱敏：</b>{@code idCardMasked} 只在前 6 位与后 4 位之间保留，中间用 * 填充。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/18 09:01
 * @since 1.0
 */
@Getter
@Builder
public class AdminUserDetailResult {

    // ===== sys_user =====
    private final Long userId;
    private final Long campusId;
    private final String username;
    private final Integer userType;
    private final Integer source;
    private final Integer status;

    // ===== user_profile =====
    private final String nickname;
    private final String phoneNumber;
    private final String email;
    private final Integer gender;
    private final LocalDate birthDate;
    private final String avatarUrl;
    private final String address;
    private final String remark;

    // ===== 扩展（按 userType 二选一，另一个为 null） =====
    private final StudentExtension student;
    private final TeacherExtension teacher;

    @Getter
    @Builder
    public static class StudentExtension {
        private final String studentNo;
        private final Long classId;
        private final Integer enrollYear;
        private final String major;
        private final String idCardMasked;
        private final String familyAddress;
    }

    @Getter
    @Builder
    public static class TeacherExtension {
        private final String teacherNo;
        private final String remark;
    }

}
