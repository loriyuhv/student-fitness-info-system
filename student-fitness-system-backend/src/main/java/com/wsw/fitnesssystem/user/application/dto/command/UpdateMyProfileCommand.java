package com.wsw.fitnesssystem.user.application.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/18 08:55
 * @since 1.0
 */
@Getter
@Builder
public class UpdateMyProfileCommand {

    private final String nickname;
    private final String phoneNumber;
    private final String email;
    private final Integer gender;
    private final LocalDate birthDate;
    private final String address;
    private final String avatarUrl;

}
