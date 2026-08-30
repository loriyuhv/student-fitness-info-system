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
     * <p>规则：</p>
     * <li>1. 用户名非空且长度不超过上限</li>
     * <li>2. 密码为空时使用默认密码</li>
     * <li>3. 昵称为空时默认使用用户名</li>
     *
     * @param username    用户名
     * @param rawPassword 原始密码（可为空）
     * @param nickname    昵称（可为空）
     * @return 合法的 User 领域对象
     * @throws BizException 当用户名非法时
     */
    public static User create(String username, String rawPassword, String nickname, int rowIndex) {
        if (StringUtils.isBlank(username)) {
            throw new BizException(ResultCode.PARAM_INVALID, "用户名不能为空");
        }
        String trimmedUsername = username.trim();
        if (trimmedUsername.length() > ExcelConstants.USERNAME_MAX_LENGTH) {
            throw new BizException(ResultCode.PARAM_INVALID, "用户名长度超过限制: " + trimmedUsername);
        }

        User user = new User();
        user.setUsername(trimmedUsername);
        user.setPassword(StringUtils.isBlank(rawPassword)
                ? ExcelConstants.DEFAULT_PASSWORD
                : rawPassword);
        user.setNickname(StringUtils.isBlank(nickname)
                ? trimmedUsername
                : nickname.trim());
        user.setRowIndex(rowIndex);
        return user;
    }

}