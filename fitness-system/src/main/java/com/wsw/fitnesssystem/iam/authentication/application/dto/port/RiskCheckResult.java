package com.wsw.fitnesssystem.iam.authentication.application.dto.port;

import lombok.Builder;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/27 11:45
 * @since 1.0
 */
@Builder
public record RiskCheckResult(int failCount, boolean locked, int remainingAttempts) {}
