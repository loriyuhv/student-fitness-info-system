package com.wsw.fitnesssystem.iam.authentication.application.port.output.dto;

import lombok.Builder;

/**
 * @param failCount         当前累计失败次数
 * @param locked            本次是否触发了锁定
 * @param remainingAttempts 剩余可尝试次数
 * @author loriyuhv
 * @version 1.0 2026/8/27 11:45
 * @since 1.0
 */
@Builder
public record RiskCheckResult(int failCount, boolean locked, int remainingAttempts) {}
