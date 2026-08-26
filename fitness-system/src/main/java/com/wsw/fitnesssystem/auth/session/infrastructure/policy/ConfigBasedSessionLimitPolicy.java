package com.wsw.fitnesssystem.auth.session.infrastructure.policy;

import com.wsw.fitnesssystem.auth.session.domain.policy.SessionLimitPolicy;
import com.wsw.fitnesssystem.auth.session.infrastructure.config.SessionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 基于配置文件的会话限制策略实现
 *
 * @author loriyuhv
 * @version 1.0 2026/8/27 07:14
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class ConfigBasedSessionLimitPolicy implements SessionLimitPolicy {

    private final SessionProperties sessionProperties;

    @Override
    public int getMaxSessions() {
        return sessionProperties.getMaxOnlineSessions();
    }

}
