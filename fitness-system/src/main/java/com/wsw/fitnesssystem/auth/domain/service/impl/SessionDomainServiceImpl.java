package com.wsw.fitnesssystem.auth.domain.service.impl;

import com.wsw.fitnesssystem.auth.domain.port.SessionRepository;
import com.wsw.fitnesssystem.auth.domain.service.SessionDomainService;
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

    @Override
    public void limitSessions(Long campusId, Long userId, int maxSessions) {
        Long size = sessionRepository.countSessions(campusId, userId);
        if (size == null || size < maxSessions) return;

        sessionRepository.getOldestSession(campusId, userId)
            .ifPresent(oldest -> sessionRepository.removeSession(
                campusId, userId, oldest)
            );
    }

    @Override
    public void verifyRefreshToken(Long campusId, Long userId, String refreshTokenId) {
        boolean exists = sessionRepository.existsRefreshToken(
                campusId,
                userId,
                refreshTokenId
        );

        if(!exists){
            throw new BizException(ResultCode.TOKEN_INVALID);
        }
    }

    @Override
    public void rotateRefreshToken(
            Long campusId,
            Long userId,
            String oldRefreshTokenId,
            String oldAccessTokenId,
            String newRefreshTokenId,
            String newAccessTokenId) {
        sessionRepository.rotateRefreshToken(
                campusId,
                userId,
                oldRefreshTokenId,
                oldAccessTokenId,
                newRefreshTokenId,
                newAccessTokenId
        );
    }
}
