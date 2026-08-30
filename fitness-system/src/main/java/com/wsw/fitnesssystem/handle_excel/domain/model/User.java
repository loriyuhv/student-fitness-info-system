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

    private String username;
    private String password;
    private String nickname;
    private transient Integer rowIndex; // 临时行号，不持久化

    /**
     * 工厂方法：强制走业务规则创建
     * <p>注意：格式校验（非空、长度）应在调用前完成，此处仅做防御性检查</p>
     *
     * @param username    用户名（已校验过非空和长度）
     * @param rawPassword 原始密码
     * @param nickname    昵称
     * @param rowIndex    行号
     * @return 合法的 User 领域对象
     */
    public static User create(String username, String rawPassword, String nickname, int rowIndex) {
        // 防御性检查：虽然是冗余，但保护领域对象不创建非法状态
        if (StringUtils.isBlank(username)) {
            throw new BizException(ResultCode.PARAM_INVALID, "用户名不能为空");
        }

        if (username.length() > ExcelConstants.USERNAME_MAX_LENGTH) {
            throw new BizException(ResultCode.PARAM_INVALID, "用户名长度超过限制: " + username);
        }

        if (rawPassword.length() < ExcelConstants.PASSWORD_MIN_LENGTH) {
            throw new BizException(
                ResultCode.PARAM_INVALID, "密码至少" + ExcelConstants.PASSWORD_MIN_LENGTH + "字符");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(rawPassword);
        user.setNickname(StringUtils.isBlank(nickname) ? username : nickname.trim());
        user.setRowIndex(rowIndex);
        return user;
    }

}