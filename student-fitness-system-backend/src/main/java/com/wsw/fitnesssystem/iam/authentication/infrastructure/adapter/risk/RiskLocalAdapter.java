package com.wsw.fitnesssystem.iam.authentication.infrastructure.adapter.risk;

import com.wsw.fitnesssystem.iam.authentication.application.port.output.dto.RiskCheckResult;
import com.wsw.fitnesssystem.iam.authentication.application.port.output.RiskPort;
import com.wsw.fitnesssystem.iam.risk.application.port.input.RiskControlUseCase;
import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskFailResult;
import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskSubject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/27 11:47
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RiskLocalAdapter implements RiskPort {

    private final RiskControlUseCase riskControlUseCase;

    @Override
    public void checkAccess(String username) {
        riskControlUseCase.checkAccess(RiskSubject.user(username));
    }

    /**
     * 容错（降级）策略设计：Fail-Open（故障放行）
     *
     * @param username 用户账号
     * @return 风控检查结果
     */
    @Override
    public RiskCheckResult recordFailure(String username) {
        try {
            RiskFailResult result = riskControlUseCase.recordFailure(RiskSubject.user(username));

            return RiskCheckResult.builder()
                .failCount(result.failCount())
                .locked(result.locked())
                .remainingAttempts(result.remainingAttempts())
                .build();
        } catch (Exception e) {
            log.error("Risk control recordFailure failed, degrade to allow login. user={}", username, e);
            // 风控挂了，默认放行（返回一个未锁定、无限制的结果）
            return RiskCheckResult.builder()
                .failCount(0)
                .locked(false)
                .remainingAttempts(Integer.MAX_VALUE)
                .build();
        }
    }

    @Override
    public void resetState(String username) {
        riskControlUseCase.resetState(RiskSubject.user(username));
    }

}
