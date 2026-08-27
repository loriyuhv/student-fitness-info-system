package com.wsw.fitnesssystem.auth.risk.application.impl;

import com.wsw.fitnesssystem.auth.risk.application.RiskControlService;
import com.wsw.fitnesssystem.auth.risk.domain.model.AccountRiskProfile;
import com.wsw.fitnesssystem.auth.risk.domain.policy.RiskLockPolicy;
import com.wsw.fitnesssystem.auth.risk.domain.port.AccountRiskRepository;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.AccountIdentifier;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.RiskFailResult;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.RiskPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 风控应用服务实现
 *
 * <p>职责边界（DDD Lite）：
 * <ul>
 *     <li>从仓储加载聚合根（只读）</li>
 *     <li>根据配置构建风控策略值对象</li>
 *     <li>调用仓储原子方法（高并发写）</li>
 *     <li>删除风控记录（重置/解封）</li>
 * </ul>
 *
 * <p>不包含业务规则，业务规则在 {@link AccountRiskProfile} 中。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:21
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskControlAppService implements RiskControlService {

    private final RiskLockPolicy riskLockPolicy;
    private final AccountRiskRepository riskRepository;

    /** 根据当前配置构建策略值对象 */
    private RiskPolicy currentPolicy() {
        return new RiskPolicy(
            riskLockPolicy.getMaxFailCount(),
            riskLockPolicy.getLockDurationSeconds(),
            riskLockPolicy.getCountWindowSeconds()
        );
    }

    @Override
    public void preCheck(String username) {
        AccountIdentifier identifier = new AccountIdentifier(username);
        AccountRiskProfile profile = riskRepository.findByIdentifier(identifier)
            .orElse(AccountRiskProfile.create(identifier));
        // 只读检查，无竞态风险
        profile.checkBeforeLogin();
    }

    @Override
    public RiskFailResult onFail(String username) {
        AccountIdentifier identifier = new AccountIdentifier(username);
        RiskPolicy policy = currentPolicy();
        // 直接调用仓储原子操作，无 Java 层竞态
        return riskRepository.incrementFailAndGet(identifier, policy);
    }

    @Override
    public void onSuccess(String username) {
        AccountIdentifier identifier = new AccountIdentifier(username);
        // 登录成功直接删除风控记录（重置）
        riskRepository.delete(identifier);
        log.debug("Reset risk state for user: {}", username);
    }

    @Override
    public void unlockAccount(String username) {
        AccountIdentifier identifier = new AccountIdentifier(username);
        riskRepository.delete(identifier);
        log.info("Admin manually unlocked account: {}", username);
    }

}
