package com.wsw.fitnesssystem.auth.authentication.application.dto.port;

import lombok.Builder;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/27 11:45
 * @since 1.0
 */
@Builder
public record RiskCheckResult(int failCount, boolean locked, int remainingAttempts) {}
