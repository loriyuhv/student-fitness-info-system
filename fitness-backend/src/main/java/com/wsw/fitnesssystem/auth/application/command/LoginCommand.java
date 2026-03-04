package com.wsw.fitnesssystem.auth.application.command;

import lombok.Builder;
import lombok.Getter;

/**
 * 登录上下文（业务输入模型）
 * 注意：
 * - 不包含 HttpServletRequest
 * - 只包含“登录业务关心的数据”
 *
 * @author loriyuhv
 * @version 1.0 2026/1/16 12:59
 * @since 1.0
 */
@Getter
@Builder
public class LoginCommand {
    private String username;
    private String password;

    /** 设备类型：WEB / APP / MINI_PROGRAM */
    private String deviceType;

    /** 客户端 IP */
    private String ip;

    /** User-Agent */
    private String userAgent;
}
