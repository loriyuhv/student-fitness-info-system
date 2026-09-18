package com.wsw.fitnesssystem.user.application.dto.result;

import lombok.Builder;
import lombok.Getter;

/**
 * 用户列表项结果（C1）。
 *
 * <p><b>结构：</b>通用字段 + 按 userType 二选一的扩展字段。</p>
 * <ul>
 *   <li>{@code userType=0}（管理员）：只填通用字段</li>
 *   <li>{@code userType=1}（教师）：通用字段 + {@code teacherNo}</li>
 *   <li>{@code userType=2}（学生）：通用字段 + {@code studentNo/classId/major}</li>
 * </ul>
 *
 * <p><b>严禁</b>添加任何 Web/JSON 序列化注解。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 18:16
 * @since 1.0
 */
@Getter
@Builder
public class UserListItemResult {

    // ===== sys_user =====
    private final Long userId;
    private final Long campusId;
    private final String username;
    private final Integer userType;
    private final Integer status;

    // ===== user_profile =====
    private final String nickname;
    private final String phoneNumber;
    private final String email;

    // ===== 学生扩展（仅 userType=2） =====
    private final String studentNo;
    private final Long classId;
    private final String major;

    // ===== 教师扩展（仅 userType=1） =====
    private final String teacherNo;

}
