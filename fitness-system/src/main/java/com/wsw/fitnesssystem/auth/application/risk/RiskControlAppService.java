package com.wsw.fitnesssystem.auth.application.risk;

import com.wsw.fitnesssystem.auth.application.service.RiskControlService;
import com.wsw.fitnesssystem.auth.domain.risk.model.AccountRiskProfile;
import com.wsw.fitnesssystem.auth.domain.risk.port.AccountRiskRepository;
import com.wsw.fitnesssystem.auth.domain.risk.valueobject.AccountIdentifier;
import com.wsw.fitnesssystem.auth.domain.risk.valueobject.RiskFailResult;
import com.wsw.fitnesssystem.auth.domain.risk.valueobject.RiskPolicy;
import com.wsw.fitnesssystem.auth.infrastructure.config.RiskPolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 风控应用服务
 *
 * <p>职责边界（DDD Lite）：
 * <ul>
 *     <li>从仓储加载聚合根</li>
 *     <li>根据配置构建风控策略值对象</li>
 *     <li>调用领域对象的行为方法</li>
 *     <li>将变更后的聚合根写回仓储</li>
 * </ul>
 *
 * <p>不包含业务规则，业务规则在 {@link AccountRiskProfile} 中。</p>
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:21
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class RiskControlAppService implements RiskControlService {
    private final AccountRiskRepository riskRepository;
    private final RiskPolicyProperties policyProperties;

    /** 根据当前配置构建策略值对象 */
    private RiskPolicy currentPolicy() {
        return RiskPolicy.fromProperties(policyProperties);
    }

    @Override
    public void preCheck(String username) {
        AccountIdentifier identifier = new AccountIdentifier(username);
        // RiskPolicy policy = currentPolicy();

        AccountRiskProfile profile = riskRepository
                .findByIdentifier(identifier)
                .orElse(new AccountRiskProfile(identifier));

        // 领域行为：检查是否被锁定
        profile.checkBeforeLogin();
    }

    @Override
    public RiskFailResult onFail(String username) {
        AccountIdentifier identifier = new AccountIdentifier(username);
        RiskPolicy policy = currentPolicy();

        AccountRiskProfile profile = riskRepository
                .findByIdentifier(identifier)
                .orElse(new AccountRiskProfile(identifier));

        // 领域行为：记录失败
        int failCount = profile.recordFailure(policy);

        // 保存领域状态
        riskRepository.save(profile);

        return new RiskFailResult(
                failCount,
                profile.getLock().isLocked(),
                profile.remainingAttempts(policy)
        );
    }

    @Override
    public void onSuccess(String username) {
        AccountIdentifier identifier = new AccountIdentifier(username);

        AccountRiskProfile profile = riskRepository
                .findByIdentifier(identifier)
                .orElse(null);

        // 从未失败过，无需处理
        if (profile == null) return;

        // 领域行为：重置
        profile.resetOnSuccess();

        // 保存
        riskRepository.save(profile);
    }
}
