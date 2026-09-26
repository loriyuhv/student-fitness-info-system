package com.wsw.fitnesssystem.iam.risk.domain.port.output;

import com.wsw.fitnesssystem.iam.risk.domain.enums.RiskDimension;
import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskPolicy;

/**
 * 风控策略提供者端口（领域层定义）。
 *
 * <p><b>职责：</b>按维度提供对应的 {@link RiskPolicy} 值对象，
 * 由 Infrastructure 层实现（例如从 {@code application.yaml} 读取）。</p>
 *
 * <p><b>为什么按维度取？</b></p>
 * <ul>
 *   <li>账号维度：3 次失败 → 锁 15 分钟</li>
 *   <li>IP 维度：30 次异常 → 限 5 分钟</li>
 *   <li>设备维度：5 次异常 → 封 30 分钟</li>
 *   <li>不同维度的策略参数天然不同，用一个全局策略无法表达</li>
 * </ul>
 *
 * <p><b>端口设计：</b>返回完整值对象而非分散的原始类型，原因：
 * <ul>
 *   <li>调用方拿到的是「保证合法的策略对象」，无需自己组装和校验</li>
 *   <li>值对象的构造期校验自动生效，避免绕过</li>
 *   <li>端口语义清晰：「按维度获取策略」返回「策略」</li>
 * </ul></p>
 *
 *
 * @author loriyuhv
 * @version 1.0 2026/8/27 12:51
 * @since 1.0
 */
public interface RiskPolicyProvider {

    /**
     * 获取指定维度的风控策略。
     *
     * @param dimension 风控维度，非 null
     * @return 对应维度的策略值对象（永远非 null，永远合法）
     * @throws IllegalStateException 若该维度缺少配置（启动期应已拦截）
     */
    RiskPolicy getPolicy(RiskDimension dimension);

}
