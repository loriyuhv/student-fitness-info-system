package com.wsw.fitnesssystem.handle_excel.domain.model;

import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ExcelConstants;

/**
 * 用户领域模型 — 充血对象
 * <p>封装用户创建的业务规则，保证对象一旦创建即合法</p>
 * <p>原则：不依赖任何框架注解，只承载业务语义与不变规则</p>
 *
 * @author loriyuhv
 * @version 2.0 2026/8/23 01:28
 * @since 1.0
 */
@Data
public class User {

    private Long campusId;
    private String username;
    private String password;
    private String nickname;
    private String phoneNumber;
    private String email;
    private Integer userType;
    private transient Integer rowIndex; // 临时行号，不持久化

    /**
     * 工厂方法：强制走业务规则创建
     * <p>注意：格式校验（非空、长度）应在调用前完成，此处仅做防御性检查</p>
     *
     * @param campusId    租户ID
     * @param username    用户名（已校验过非空和长度）
     * @param rawPassword 原始密码
     * @param nickname    昵称
     * @param phoneNumber 手机号码
     * @param email       邮箱
     * @param userType    用户类型 0 管理员 1 教师 2 学生
     * @param rowIndex    行号
     * @return 合法的 User 领域对象
     */
    public static User create(
        Long campusId, String username, String rawPassword, String nickname,
        String phoneNumber, String email, Integer userType, int rowIndex) {
        // 防御性检查：虽然是冗余，但保护领域对象不创建非法状态

        // ====== 必填校验 ======
        if (campusId == null) {
            throw new BizException(ResultCode.PARAM_INVALID, "校区不能为空");
        }
        if (StringUtils.isBlank(username)) {
            throw new BizException(ResultCode.PARAM_INVALID, "用户名不能为空");
        }
        if (StringUtils.isBlank(rawPassword) || rawPassword.length() < ExcelConstants.PASSWORD_MIN_LENGTH) {
            throw new BizException(ResultCode.PARAM_INVALID,
                "密码至少" + ExcelConstants.PASSWORD_MIN_LENGTH + "字符");
        }
        if (userType == null) {
            throw new BizException(ResultCode.PARAM_INVALID, "用户类型不能为空");
        }

        // ====== 长度校验 ======
        String trimmedUsername = username.trim();
        if (trimmedUsername.length() > ExcelConstants.USERNAME_MAX_LENGTH) {
            throw new BizException(ResultCode.PARAM_INVALID,
                "用户名长度超过限制: " + trimmedUsername);
        }

        User user = new User();
        user.setCampusId(campusId);
        user.setUsername(trimmedUsername);
        user.setPassword(rawPassword); // 明文密码，后续由 Assembler 加密
        user.setNickname(StringUtils.isBlank(nickname) ? trimmedUsername : nickname.trim());
        user.setPhoneNumber(StringUtils.isBlank(phoneNumber) ? null : phoneNumber.trim());
        user.setEmail(StringUtils.isBlank(email) ? null : email.trim().toLowerCase());
        user.setUserType(userType);
        user.setRowIndex(rowIndex);
        return user;
    }

}