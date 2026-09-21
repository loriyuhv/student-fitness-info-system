package com.wsw.fitnesssystem.iam.session.infrastructure.config;

import com.wsw.fitnesssystem.iam.session.domain.policy.SessionLimitPolicy;
import com.wsw.fitnesssystem.iam.session.domain.repository.SessionRepository;
import com.wsw.fitnesssystem.iam.session.domain.service.SessionDomainService;
import com.wsw.fitnesssystem.iam.session.domain.service.impl.DefaultSessionDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 会话领域服务装配配置
 *
 * <p><b>存在意义：</b>领域层不依赖 Spring，因此 {@code DefaultSessionDomainService}
 * 上没有 {@code @Service} 注解；本配置类负责在基础设施层完成 Bean 装配，
 * 使领域层保持纯 POJO。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21 14:52
 * @since 1.0
 */
@Configuration
public class SessionDomainConfiguration {

    @Bean
    public SessionDomainService sessionDomainService(
        SessionRepository sessionRepository, SessionLimitPolicy sessionLimitPolicy
    ) {
        return new DefaultSessionDomainService(sessionRepository, sessionLimitPolicy);
    }

}
