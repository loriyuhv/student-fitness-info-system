package com.wsw.fitnesssystem.iam.risk.domain.port.output;

import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskLockPolicy;

/**
 * 风控锁定策略提供者端口（领域层定义）。
 *
 * <p><b>职责：</b>向外提供风控策略值对象。由 Infrastructure 层实现。</p>
 *
 * <p><b>端口设计：</b>返回完整的 {@link RiskLockPolicy} 值对象，
 * 而非分散的原始类型。原因：</p>
 * <ul>
 *   <li>调用方拿到的是「保证合法的策略对象」，无需自己组装和校验</li>
 *   <li>值对象的构造期校验自动生效，避免绕过</li>
 *   <li>端口语义清晰：「获取策略」返回「策略」</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/27 12:51
 * @since 1.0
 */
public interface RiskLockPolicyProvider {
    /**
     * 获取当前风控锁定策略。
     *
     * @return 风控策略值对象（永远非 null，永远合法）
     */
    RiskLockPolicy getPolicy();

}
