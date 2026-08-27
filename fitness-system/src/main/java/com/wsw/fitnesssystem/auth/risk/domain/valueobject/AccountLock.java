package com.wsw.fitnesssystem.auth.risk.domain.valueobject;

/**
 * 账号锁定状态 - 值对象
 *
 * <p>不可变。领域层只表达"是否锁定"，不感知 Redis TTL 等技术细节。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:02
 * @since 1.0
 */
public record AccountLock(boolean status) {

    public static AccountLock unlocked() {
        return new AccountLock(false);
    }

    public static AccountLock locked() {
        return new AccountLock(true);
    }

}
