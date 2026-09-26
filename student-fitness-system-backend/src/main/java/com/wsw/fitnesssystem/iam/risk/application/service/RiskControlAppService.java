package com.wsw.fitnesssystem.iam.risk.application.service;

import com.wsw.fitnesssystem.iam.risk.application.port.input.RiskControlUseCase;
import com.wsw.fitnesssystem.iam.risk.domain.enums.RiskDimension;
import com.wsw.fitnesssystem.iam.risk.domain.model.RiskProfile;
import com.wsw.fitnesssystem.iam.risk.domain.port.output.RiskPolicyProvider;
import com.wsw.fitnesssystem.iam.risk.domain.repository.RiskRepository;
import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskFailResult;
import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskPolicy;
import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskSubject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 风控应用服务实现
 *
 * <p><b>职责边界：</b>
 * <ul>
 *   <li>从仓储加载聚合根（只读）</li>
 *   <li>按主体维度获取对应策略值对象</li>
 *   <li>调用仓储原子方法（高并发写）</li>
 *   <li>删除风控记录（重置 / 解封）</li>
 * </ul></p>
 *
 * <p><b>不含业务规则</b>：规则判断在 {@link RiskProfile} 与
 * {@link com.wsw.fitnesssystem.iam.risk.domain.repository.RiskRepository}
 * 的原子脚本中。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:21
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskControlAppService implements RiskControlUseCase {

    private final RiskPolicyProvider riskPolicyProvider;
    private final RiskRepository riskRepository;

    /**
     * 取指定维度当前生效的策略值对象。
     *
     * <p>策略由 Provider 按维度提供，本方法仅做维度透传，
     * 不缓存、不校验（值对象已保证合法）。</p>
     */
    private RiskPolicy currentPolicy(RiskDimension dimension) {
        return riskPolicyProvider.getPolicy(dimension);
    }

    @Override
    public void preCheck(RiskSubject subject) {
        RiskProfile profile = riskRepository.find(subject)
            .orElse(RiskProfile.create(subject));
        // 只读检查，无竞态风险
        profile.checkBeforeAccess();
    }

    @Override
    public RiskFailResult onFail(RiskSubject subject) {
        RiskPolicy policy = currentPolicy(subject.dimension());
        // 直接调用仓储原子操作，Java 层无竞态
        RiskFailResult result = riskRepository.recordFailure(subject, policy);
        if (result.newlyLocked()) {
            // 发布领域事件，交由监听器处理（审计、告警、通知等）
            log.info("risk fail result: {}", result);
        }
        return result;
    }

    @Override
    public void onSuccess(RiskSubject subject) {
        riskRepository.delete(subject);
        log.debug("Reset risk state: dimension={}, value={}",
            subject.dimension(), subject.value());
    }

    @Override
    public void unlock(RiskSubject subject) {
        riskRepository.delete(subject);
        log.info("Admin manually unlocked: dimension={}, value={}",
            subject.dimension(), subject.value());
    }

}
