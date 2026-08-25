package com.wsw.fitnesssystem.auth.authentication.application.service;

import com.wsw.fitnesssystem.auth.authentication.application.command.LoginCommand;
import com.wsw.fitnesssystem.auth.authentication.domain.model.AuthUser;
import com.wsw.fitnesssystem.auth.authentication.application.dto.TokenPair;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 14:06
 * @since 1.0
 */
public interface LoginSuccessProcessor {
    void process(AuthUser user, LoginCommand cmd, TokenPair tokenPair);
}
