package com.wsw.fitnesssystem.auth.authentication.application.dto.command;

import lombok.Builder;
import lombok.Getter;

/**
 * 刷新令牌业务指令（Application 层输入模型）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>承载刷新令牌操作的业务输入数据</li>
 *   <li>极简设计，只包含 refreshToken 字符串</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 10:45
 * @since 1.0
 */
@Getter
@Builder
public class RefreshCommand {

    /** 刷新令牌字符串 */
    private String refreshToken;

}
