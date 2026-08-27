package com.wsw.fitnesssystem.auth.risk.domain.model;

import com.wsw.fitnesssystem.auth.risk.domain.valueobject.AccountIdentifier;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.AccountLock;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.RiskPolicy;
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

    /** 私有构造函数 */
    private AccountRiskProfile(AccountIdentifier identifier, int failCount, AccountLock lock) {
        this.identifier = identifier;
        this.consecutiveFailCount = Math.max(0, failCount);
        this.lock = lock;
    }

    /** 新建账号风控画像 （初始状态） */
    public static AccountRiskProfile newProfile(AccountIdentifier identifier) {
        return new AccountRiskProfile(identifier, 0, AccountLock.unlocked());
    }

    /** 从仓储恢复 */
    public static AccountRiskProfile restore(
        AccountIdentifier identifier, int failCount, AccountLock lock
    ) {
        // 防御：失败次数不能为负数
        if (failCount < 0) {
            failCount = 0;
        }

        // 防御：已锁定但失败次数为0（不太可能，但防止脏数据）
        if (lock.isLocked() && failCount == 0) {
            failCount = 1;
        }

        return new AccountRiskProfile(identifier, failCount, lock);
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
