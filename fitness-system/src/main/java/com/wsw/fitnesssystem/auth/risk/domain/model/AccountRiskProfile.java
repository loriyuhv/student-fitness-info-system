package com.wsw.fitnesssystem.auth.risk.domain.model;

import com.wsw.fitnesssystem.auth.risk.domain.valueobject.AccountIdentifier;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.AccountLock;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.Getter;

/**
 * 账号风控画像 - 聚合根（只读视图）
 *
 * <p>职责边界：
 * <ul>
 *     <li>维护账号的登录失败次数和锁定状态（只读）</li>
 *     <li>登录前检查是否被锁定</li>
 * </ul>
 *
 * <p>注意：写操作（失败计数 + 锁定）已移至 Infrastructure 层原子脚本，
 * 本聚合根不再包含 recordFailure() 方法。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:11
 * @since 1.0
 */
@Getter
public class AccountRiskProfile {

    private final AccountIdentifier identifier;
    private final int consecutiveFailCount;
    private final AccountLock lock;

    /** 私有构造函数 保证不可变 */
    private AccountRiskProfile(AccountIdentifier identifier, int failCount, AccountLock lock) {
        this.identifier = identifier;
        this.consecutiveFailCount = Math.max(0, failCount);
        this.lock = lock;
    }

    /** 新建账号风控画像 （初始状态） */
    public static AccountRiskProfile create(AccountIdentifier identifier) {
        return new AccountRiskProfile(identifier, 0, AccountLock.unlocked());
    }

    /** 从仓储恢复 */
    public static AccountRiskProfile restore(
        AccountIdentifier identifier, int failCount, AccountLock lock
    ) {
        if (lock == null) {
            lock = AccountLock.unlocked();
        }

        // 防御：失败次数不能为负数
        if (failCount < 0) {
            failCount = 0;
        }

        // 防御：已锁定但失败次数为0（不太可能，但防止脏数据）
        if (lock.status() && failCount == 0) {
            failCount = 1;
        }

        return new AccountRiskProfile(identifier, failCount, lock);
    }

    /**
     * 登录前检查（只读操作）
     *
     * @throws BizException 账号被锁定时抛出
     */
    public void checkBeforeLogin() {
        if (lock.status()) {
            throw new BizException(ResultCode.AUTH_ACCOUNT_LOCKED);
        }
    }

}
