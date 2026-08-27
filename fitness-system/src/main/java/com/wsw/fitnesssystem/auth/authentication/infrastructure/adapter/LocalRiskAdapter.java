package com.wsw.fitnesssystem.auth.authentication.infrastructure.adapter;

import com.wsw.fitnesssystem.auth.authentication.application.dto.port.RiskCheckResult;
import com.wsw.fitnesssystem.auth.authentication.application.port.RiskPort;
import com.wsw.fitnesssystem.auth.risk.application.RiskControlService;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.RiskFailResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/27 11:47
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class LocalRiskAdapter implements RiskPort {

    private final RiskControlService riskControlService;

    @Override
    public void preCheck(String username) {
        riskControlService.preCheck(username);
    }

    @Override
    public RiskCheckResult onFail(String username) {
        RiskFailResult result = riskControlService.onFail(username);

        return RiskCheckResult.builder()
            .failCount(result.getFailCount())
            .locked(result.isLocked())
            .remainingAttempts(result.getRemainingAttempts())
            .build();
    }

    @Override
    public void onSuccess(String username) {
        riskControlService.onSuccess(username);
    }

}
