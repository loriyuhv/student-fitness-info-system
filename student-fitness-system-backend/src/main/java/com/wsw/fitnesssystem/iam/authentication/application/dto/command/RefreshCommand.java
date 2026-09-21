package com.wsw.fitnesssystem.iam.authentication.application.dto.command;

/**
 * 刷新令牌业务指令（Application 层输入模型）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>承载刷新令牌操作的业务输入数据</li>
 * </ul>
 *
 * @param refreshToken
 * @param deviceType
 * @param userAgent
 * @param ip
 * @author loriyuhv
 * @version 1.0 2026/8/26 10:45
 * @since 1.0
 */
public record RefreshCommand(
    String refreshToken,
    String deviceType,
    String userAgent,
    String ip
) {
}
