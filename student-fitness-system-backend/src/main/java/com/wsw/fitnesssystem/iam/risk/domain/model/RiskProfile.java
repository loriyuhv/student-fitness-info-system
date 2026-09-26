package com.wsw.fitnesssystem.iam.risk.domain.model;

import com.wsw.fitnesssystem.iam.risk.domain.vb.LockState;
import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskSubject;
import com.wsw.fitnesssystem.shared.domain.exception.DomainStateException;
import lombok.Getter;

/**
 * 风控画像 - 聚合根（只读视图）
 *
 * <p><b>职责边界：</b>
 * <ul>
 *   <li>维护某个 {@link RiskSubject} 的失败计数与锁定状态（只读）</li>
 *   <li>提供登录/访问前检查</li>
 * </ul></p>
 *
 * <p><b>写操作说明：</b>失败计数 +1、触发锁定等并发写操作
 * 已下沉到 Infrastructure 层的 Lua 原子脚本，
 * 本聚合根不包含 {@code recordFailure()} 类方法，避免「读-改-写」竞态。</p>
 *
 * <p><b>通用性：</b>本聚合根不关心主体是账号、IP 还是设备，
 * 只依赖 {@link RiskSubject} 与 {@link LockState} 的抽象。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/26 07:56
 * @since 1.0
 */
@Getter
public final class RiskProfile {

    /** 风控主体（维度 + 标识值） */
    private final RiskSubject subject;

    /** 连续失败次数 */
    private final int consecutiveFailCount;

    /** 当前锁定状态 */
    private final LockState lock;

    /** 私有构造，保证不可变 */
    private RiskProfile(RiskSubject subject, int failCount, LockState lock) {
        this.subject = subject;
        this.consecutiveFailCount = Math.max(0, failCount);
        this.lock = lock;
    }

    // ==================== 工厂方法 ====================

    /**
     * 新建风控画像（初始状态：0 次失败、未锁定）。
     */
    public static RiskProfile create(RiskSubject subject) {
        return new RiskProfile(subject, 0, LockState.unlocked());
    }

    /**
     * 从仓储恢复。
     *
     * <p>包含对脏数据的防御性修正：
     * <ul>
     *   <li>失败次数为负 → 修正为 0</li>
     *   <li>已锁定但失败次数为 0 → 修正为 1（保持语义一致）</li>
     * </ul></p>
     */
    public static RiskProfile restore(RiskSubject subject, int failCount, LockState lock) {
        if (lock == null) {
            lock = LockState.unlocked();
        }
        if (failCount < 0) {
            failCount = 0;
        }
        if (lock.status() && failCount == 0) {
            failCount = 1;
        }
        return new RiskProfile(subject, failCount, lock);
    }

    // ==================== 领域行为 ====================

    /**
     * 访问前检查（只读）。
     *
     * <p>不修改状态，仅根据当前锁定状态决定是否放行。
     * 并发场景下没有写操作，因此无竞态。</p>
     *
     * @throws DomainStateException 主体处于锁定状态时抛出
     */
    public void checkBeforeAccess() {
        if (lock.status()) {
            throw new DomainStateException("主体已处于风控锁定状态: " + subject.value());
        }
    }

}
