package com.wsw.fitnesssystem.auth.domain.risk.model;

import com.wsw.fitnesssystem.auth.domain.risk.valueobject.AccountIdentifier;
import com.wsw.fitnesssystem.auth.domain.risk.valueobject.AccountLock;
import com.wsw.fitnesssystem.auth.domain.risk.valueobject.RiskPolicy;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.Getter;

/**
 * 账号风控画像 - 聚合根
 *
 * <p>职责边界：
 * <ul>
 *     <li>维护账号的登录失败次数和锁定状态</li>
 *     <li>根据风控策略判断当前登录是否被允许</li>
 *     <li>记录失败、重置状态、执行锁定（领域行为）</li>
 * </ul>
 *
 * <p>不变量：
 * <ol>
 *     <li>被锁定的账号不允许登录</li>
 *     <li>失败次数达到阈值时自动触发锁定</li>
 *     <li>登录成功时重置所有状态</li>
 * </ol>
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:11
 * @since 1.0
 */
@Getter
public class AccountRiskProfile {
    private final AccountIdentifier identifier;
    private int consecutiveFailCount;
    private AccountLock lock;

    /** 新建账号风控画像 */
    public AccountRiskProfile(AccountIdentifier identifier) {
        this.identifier = identifier;
        this.consecutiveFailCount = 0;
        this.lock = AccountLock.unlocked();
    }

    /** 从仓储恢复 */
    public AccountRiskProfile(AccountIdentifier identifier, int failCount, AccountLock lock) {
        this.identifier = identifier;
        this.consecutiveFailCount = failCount;
        this.lock = lock;
    }

    /**
     * 登录前检查
     *
     * @throws BizException 账号被锁定时抛出
     */
    public void checkBeforeLogin() {
        if (lock.isLocked()) {
            throw new BizException(ResultCode.ACCOUNT_LOCKED);
        }
    }

    /**
     * 记录一次登录失败
     *
     * @param policy 风控策略
     * @return 本次失败后的累计次数
     */
    public int recordFailure(RiskPolicy policy) {
        this.consecutiveFailCount++;

        // 只有未锁定时，才检查是否达到锁定阈值
        if (!lock.isLocked() && consecutiveFailCount >= policy.maxFailCount()) {
            this.lock = AccountLock.locked();
        }

        return this.consecutiveFailCount;
    }

    /**
     * 登录成功，重置风控状态
     */
    public void resetOnSuccess() {
        this.consecutiveFailCount = 0;
        this.lock = AccountLock.unlocked();
    }

    /**
     * 计算剩余可尝试次数
     */
    public int remainingAttempts(RiskPolicy policy) {
        return Math.max(0, policy.maxFailCount() - consecutiveFailCount);
    }
}
