package com.wsw.fitnesssystem.auth.application.service;

import com.wsw.fitnesssystem.auth.application.authentication.command.LoginCommand;
import com.wsw.fitnesssystem.auth.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.application.dto.TokenPair;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 14:06
 * @since 1.0
 */
public interface LoginSuccessProcessor {
    void process(AuthUser user, LoginCommand cmd, TokenPair tokenPair);
}
