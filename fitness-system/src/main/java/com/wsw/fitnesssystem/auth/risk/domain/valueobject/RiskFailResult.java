package com.wsw.fitnesssystem.auth.risk.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录失败处理结果 - 值对象
 *
 * <p>由领域层产出，交给应用层决定如何响应/审计。</p>
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:10
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public class RiskFailResult {
    /** 当前累计失败次数 */
    private final int failCount;

    /** 本次是否触发了锁定 */
    private final boolean locked;

    /** 剩余可尝试次数 */
    private final int remainingAttempts;
}
