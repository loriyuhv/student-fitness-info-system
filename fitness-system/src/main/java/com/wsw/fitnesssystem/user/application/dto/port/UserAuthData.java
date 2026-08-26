package com.wsw.fitnesssystem.user.application.dto.port;

import lombok.Builder;
import lombok.Getter;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/26 14:27
 * @since 1.0
 */
@Getter
@Builder
public class UserAuthData {

    private Long userId;
    private Long campusId;
    private String username;
    private String password;
    private Integer userType;
    private Integer status;

}
