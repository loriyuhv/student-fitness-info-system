package com.wsw.fitnesssystem.iam.authentication.application.dto.result;

import lombok.Builder;
import lombok.Getter;

/**
 * 用户凭证查询结果。
 *
 * <p><b>契约约束：</b></p>
 * <ul>
 *   <li>只包含认证必需字段，不含档案字段（昵称/头像/地址等）</li>
 *   <li>{@code passwordHash} 为 BCrypt 密文，<b>严禁</b>打日志或写入 HTTP 响应</li>
 *   <li>跨模块传输，不添加任何 Web 序列化注解</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 15:13
 * @since 1.0
 */
@Getter
@Builder
public class UserCredentialResult {

    private Long userId;

    private Long campusId;

    private String username;

    /** BCrypt 密文（非明文密码） */
    private String passwordHash;

    /** 0-管理员 1-教师 2-学生 */
    private Integer userType;

    /** 0-禁用 1-启用 */
    private Integer status;

    /**
     * 脱敏 toString，防止密码哈希泄露到日志。
     */
    @Override
    public String toString() {
        return "UserCredentialResult{" +
            "userId=" + userId +
            ", campusId=" + campusId +
            ", username='" + username + '\'' +
            ", passwordHash='***'" +
            ", userType=" + userType +
            ", status=" + status +
            '}';
    }

}
