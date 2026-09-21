package com.wsw.fitnesssystem.iam.authentication.application.dto.command;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/21 11:54
 * @since 1.0
 */
public record LogoutCommand(
    long userId,
    long campusId,
    String accessTokenId
) {
}
