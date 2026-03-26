package com.wsw.fitnesssystem.auth.interfaces.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/22 16:29
 * @since 1.0
 */
@Data
public class RefreshRequest {
    @NotBlank(message = "Refresh Token不能为空")
    private String refreshToken;
}
