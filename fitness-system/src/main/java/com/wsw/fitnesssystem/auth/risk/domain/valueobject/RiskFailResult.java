package com.wsw.fitnesssystem.auth.risk.domain.valueobject;

/**
 * 登录失败处理结果 - 值对象
 *
 * <p>由领域层产出，交给应用层决定如何响应/审计。</p>
 *
 * @param failCount         当前累计失败次数
 * @param locked            本次是否触发了锁定
 * @param remainingAttempts 剩余可尝试次数
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:10
 * @since 1.0
 */
public record RiskFailResult(int failCount, boolean locked, int remainingAttempts) {}
