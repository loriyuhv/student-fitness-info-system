package com.wsw.fitnesssystem.user.application.dto.command;

import lombok.Builder;
import lombok.Getter;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/16 12:00
 * @since 1.0
 */
@Getter
@Builder
public class UserAccountProvisionCommand {

    private Long campusId;
    private String username;
    private String passwordHash;
    private Integer userType;
    private Integer source;
    private Long operatorId;

}
