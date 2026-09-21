package com.wsw.fitnesssystem.iam.session.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 会话模块配置属性（基础设施层配置）
 *
 * <p><b>职责：</b>绑定 {@code iam.session.*} 下的会话存储与黑名单相关参数。</p>
 *
 * <p><b>时间单位约定：</b>所有时间字段以 <b>秒</b> 为单位，
 * 需要毫秒时通过 {@code getXxxMillis()} 派生方法获取，避免调用方各自做单位换算。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/3/21 10:42
 * @since 1.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "iam.session")
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
