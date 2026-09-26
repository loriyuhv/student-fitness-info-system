package com.wsw.fitnesssystem.iam.authentication.infrastructure.adapter.risk;

import com.wsw.fitnesssystem.iam.authentication.application.port.output.dto.RiskCheckResult;
import com.wsw.fitnesssystem.iam.authentication.application.port.output.RiskPort;
import com.wsw.fitnesssystem.iam.risk.application.port.input.RiskControlUseCase;
import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskFailResult;
import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskSubject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/27 11:47
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class RiskLocalAdapter implements RiskPort {

    private final RiskControlUseCase riskControlService;

    @Override
    public void preCheck(String username) {
        riskControlService.preCheck(RiskSubject.user(username));
    }

    @Override
    public RiskCheckResult onFail(String username) {
        RiskFailResult result = riskControlService.onFail(RiskSubject.user(username));

        return RiskCheckResult.builder()
            .failCount(result.failCount())
            .locked(result.locked())
            .remainingAttempts(result.remainingAttempts())
            .build();
    }

    @Override
    public void onSuccess(String username) {
        riskControlService.onSuccess(RiskSubject.user(username));
    }

}
