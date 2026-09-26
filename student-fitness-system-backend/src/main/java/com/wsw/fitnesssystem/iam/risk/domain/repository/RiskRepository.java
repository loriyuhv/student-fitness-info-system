package com.wsw.fitnesssystem.iam.risk.domain.repository;

import com.wsw.fitnesssystem.iam.risk.domain.model.RiskProfile;
import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskFailResult;
import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskPolicy;
import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskSubject;

import java.util.Optional;

/**
 * 风控仓储接口 - 领域端口
 *
 * <p><b>职责：</b>定义对风控状态持久化的契约，由基础设施层实现。
 * 抽象层面不区分维度，维度信息封装在 {@link RiskSubject} 中。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/26 08:01
 * @since 1.0
 */
public interface RiskRepository {

    /**
     * 查询风控画像（用于访问前检查、管理员查看）。
     *
     * @param subject 风控主体
     * @return 风控画像，无记录时返回 {@link Optional#empty()}
     */
    Optional<RiskProfile> find(RiskSubject subject);

    /**
     * <p>原子操作：失败计数 +1，并判断是否达到锁定阈值。</p>
     *
     * <p><b>并发保证：</b>这是高并发路径的唯一入口，
     * 全程在 Redis 服务端通过 Lua 脚本执行，Java 层无竞态。</p>
     *
     * @param subject 风控主体
     * @param policy  该维度对应的策略
     * @return 风控失败结果（失败次数 / 是否锁定 / 剩余尝试次数）
     */
    RiskFailResult incrementFailAndGet(RiskSubject subject, RiskPolicy policy);

    /**
     * 删除风控记录（重置状态、管理员解封时调用）。
     *
     * @param subject 风控主体
     */
    void delete(RiskSubject subject);

}
