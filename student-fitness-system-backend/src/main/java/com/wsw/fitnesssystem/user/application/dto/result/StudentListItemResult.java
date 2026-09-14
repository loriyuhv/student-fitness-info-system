package com.wsw.fitnesssystem.user.application.dto.result;

import lombok.Builder;
import lombok.Getter;

/**
 * 学生列表项结果（应用层）。
 *
 * <p><b>职责：</b>聚合来自 {@code student_profile} 与 {@code sys_user} 的字段。</p>
 * <p><b>严禁</b>添加任何 Web/JSON 序列化注解。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 18:16
 * @since 1.0
 */
@Getter
@Builder
public class StudentListItemResult {

    // ===== 来自 student_profile =====
    private final Long studentId;
    private final Long userId;
    private final String studentNo;
    private final Long classId;
    private final Integer enrollYear;
    private final String major;
    private final Integer gender;
    private final String familyAddress;

    // ===== 来自 sys_user =====
    private final String nickname;
    private final String phoneNumber;
    private final String email;

}
