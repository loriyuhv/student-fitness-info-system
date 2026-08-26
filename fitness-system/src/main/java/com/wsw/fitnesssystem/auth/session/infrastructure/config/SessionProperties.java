package com.wsw.fitnesssystem.auth.session.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 10:42
 * @since 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth.session")
public class SessionProperties {

    /**
     * 会话过期时间（单位：秒）
     * 适用：ZSet、Hash
     */
    private long sessionExpire;

    /**
     * 黑名单过期时间（单位：秒）
     */
    private long blacklistExpire;

    /**
     * 单账号最大在线会话数
     */
    private int maxOnlineSessions;

    /**
     * 获取会话过期时间，转换为毫秒
     * @return 会话过期时间（毫秒）
     */
    public long getSessionExpireMillis() {
        return sessionExpire * 1000L;
    }

    /**
     * 获取黑名单过期时间，转换为毫秒
     * @return 黑名单过期时间（毫秒）
     */
    public long getBlacklistExpireMillis() {
        return blacklistExpire * 1000L;
    }

}
