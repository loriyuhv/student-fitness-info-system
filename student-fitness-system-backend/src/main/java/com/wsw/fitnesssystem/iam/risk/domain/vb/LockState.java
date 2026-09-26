package com.wsw.fitnesssystem.iam.risk.domain.vb;

/**
 * 锁定状态 - 值对象
 *
 * <p><b>职责：</b>仅表达「是否处于锁定状态」这一领域概念，
 * 不感知 Redis TTL、锁定时长等技术细节（那些属于基础设施层）。</p>
 *
 * <p><b>命名说明：</b>原名 {@code AccountLock} 带有账号语义；
 * 泛化后所有维度共享同一状态语义，故改名为 {@code LockState}。</p>
 *
 * @param status true = 已锁定，false = 未锁定
 * @author loriyuhv
 * @version 1.0 2026/9/26 07:51
 * @since 1.0
 */
public record LockState(boolean status) {

    public static LockState unlocked() {
        return new LockState(false);
    }

    public static LockState locked() {
        return new LockState(true);
    }
}
