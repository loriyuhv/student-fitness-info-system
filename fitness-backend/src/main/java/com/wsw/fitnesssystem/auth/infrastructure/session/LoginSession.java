package com.wsw.fitnesssystem.auth.infrastructure.session;

import lombok.Builder;
import lombok.Getter;

/**
 * 一次登录会话的聚合结果
 *
 * @author loriyuhv
 * @version 1.0 2026/1/16 12:39
 * @since 1.0
 */
@Getter
@Builder
public class LoginSession {
    private String accessToken;
    private String refreshToken;
    private String tokenId;
    private long expire; // accessToken 过期时间（毫秒）
}
