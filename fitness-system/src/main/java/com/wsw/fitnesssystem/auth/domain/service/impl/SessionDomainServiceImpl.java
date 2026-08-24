package com.wsw.fitnesssystem.auth.domain.service.impl;

import com.wsw.fitnesssystem.auth.domain.port.SessionRepository;
import com.wsw.fitnesssystem.auth.domain.service.SessionDomainService;
import com.wsw.fitnesssystem.auth.infrastructure.config.SessionProperties;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 10:59
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class SessionDomainServiceImpl implements SessionDomainService {

    private final SessionRepository sessionRepository;
    private final SessionProperties sessionProperties;

    @Override
    public void limitSessions(Operator operator) {
        Long size = sessionRepository.countSessions(operator);
        int maxSessions = sessionProperties.getMaxOnlineSessions();
        if (size == null || size < maxSessions) return;

        sessionRepository.getOldestSession(operator)
                .ifPresent(oldest -> sessionRepository.removeSession(operator, oldest)
            );
    }

    @Override
    public void verifyRefreshToken(Operator operator, String refreshTokenId) {
        boolean exists = sessionRepository.existsRefreshToken(
                operator, refreshTokenId);

        if(!exists){
            throw new BizException(ResultCode.TOKEN_INVALID);
        }
    }

    @Override
    public void rotateRefreshToken(
            Operator operator,
            String oldRefreshTokenId,
            String oldAccessTokenId,
            String newRefreshTokenId,
            String newAccessTokenId) {
        sessionRepository.rotateRefreshToken(
                operator,
                oldRefreshTokenId,
                oldAccessTokenId,
                newRefreshTokenId,
                newAccessTokenId
        );
    }

}
