package com.wsw.fitnesssystem.user.application.dto.result;

import lombok.Builder;
import lombok.Getter;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/16 12:01
 * @since 1.0
 */
@Getter
@Builder
public class UserAccountProvisionResult {

    private Long userId;
    private String username;
    private Integer status;

}
