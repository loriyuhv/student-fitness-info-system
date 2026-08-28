package com.wsw.fitnesssystem.auth.authentication.interfaces.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新令牌请求 DTO（Web 层面向 HTTP 协议的输入模型）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>接收前端传递的 refreshToken</li>
 *   <li>做基本的非空校验</li>
 *   <li>前端传递的是裸 token 字符串，由 Web 层接收后转为 Application 层的 Command</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/3/22 16:29
 * @since 1.0
 */
@Data
public class RefreshRequest {

    /**
     * 刷新令牌
     * 必填，由前端在 accessToken 过期后传递
     */
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;


    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

}
