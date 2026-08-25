package com.wsw.fitnesssystem.auth.audit.domain.valueobject;

import java.time.LocalDateTime;

/**
 * Token 快照（仅成功时存在）
 *
 * @author loriyuhv
 * @version 1.0 2026/8/25 11:38
 * @since 1.0
 */
public record TokenSnapshot(String tokenId, LocalDateTime expireTime) {
}
