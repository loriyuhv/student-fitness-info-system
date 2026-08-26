package com.wsw.fitnesssystem.auth.authentication.application.service;

import com.wsw.fitnesssystem.auth.authentication.application.dto.command.LoginCommand;
import com.wsw.fitnesssystem.auth.authentication.application.dto.TokenPair;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 14:06
 * @since 1.0
 */
public interface LoginSuccessProcessor {
    void process(Operator operator, LoginCommand cmd, TokenPair tokenPair);
}
