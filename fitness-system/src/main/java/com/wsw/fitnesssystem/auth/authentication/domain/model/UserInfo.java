package com.wsw.fitnesssystem.auth.authentication.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/5 08:21
 * @since 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    /** 用户ID */
    private Long userId;

    /** 校区ID */
    private Long campusId;

    /** 登录账号（学号 / 工号） */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phoneNumber;

    /** 邮箱 */
    private String email;

    /** 备注 */
    private String remark;

    /** 用户类型：0-管理员 1-教师 2-学生 */
    private Integer userType;

    /** 来源 */
    private Integer source;
}
