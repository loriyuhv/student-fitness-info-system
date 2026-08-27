package com.wsw.fitnesssystem.auth.authentication.infrastructure.adapter;

import com.wsw.fitnesssystem.auth.authentication.application.port.SessionPort;
import com.wsw.fitnesssystem.auth.session.domain.port.SessionRepository;
import com.wsw.fitnesssystem.auth.session.domain.service.SessionDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 本地会话适配器（单体/模块化阶段使用）
 *
 * <p>通过直接调用 Session 模块的 {@link SessionRepository} 和 {@link SessionDomainService}
 * 实现会话管理功能。
 *
 * @author loriyuhv
 * @version 1.0 2026/8/27 10:47
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class LocalSessionAdapter implements SessionPort {

    private final SessionRepository sessionRepository;
    private final SessionDomainService sessionDomainService;

    @Override
    public void saveSession(long campusId, long userId, String accessTokenId, String refreshTokenId) {
        sessionRepository.saveSession(campusId, userId, accessTokenId, refreshTokenId);
    }

    @Override
    public void removeSession(long campusId, long userId, String accessTokenId) {
        sessionRepository.removeSession(campusId, userId, accessTokenId);
    }

    @Override
    public Set<String> removeAllSessions(long campusId, long userId) {
        return sessionRepository.removeAllSessions(campusId, userId);
    }

    @Override
    public boolean isOnline(long campusId, long userId, String accessTokenId) {
        return sessionRepository.isOnline(campusId, userId, accessTokenId);
    }

    @Override
    public void addToBlacklist(String accessTokenId) {
        sessionRepository.addToBlacklist(accessTokenId);
    }

    @Override
    public boolean isBlacklisted(String accessTokenId) {
        return sessionRepository.isBlacklisted(accessTokenId);
    }

    @Override
    public long getTokenVersion(long campusId, long userId) {
        return sessionRepository.getTokenVersion(campusId, userId);
    }

    @Override
    public boolean existsRefreshToken(long campusId, long userId, String refreshTokenId) {
        return sessionRepository.existsRefreshToken(campusId, userId, refreshTokenId);
    }

    @Override
    public String getAccessTokenIdByRefreshTokenId(long campusId, long userId, String refreshTokenId) {
        return sessionRepository.getAccessTokenIdByRefreshTokenId(campusId, userId, refreshTokenId);
    }

    @Override
    public void rotateRefreshToken(
        long campusId, long userId, String oldRefreshTokenId,
        String oldAccessTokenId, String newRefreshTokenId, String newAccessTokenId
    ) {
        sessionRepository.rotateRefreshToken(
            campusId, userId, oldAccessTokenId,
            oldRefreshTokenId, newAccessTokenId, newAccessTokenId
        );
    }

    @Override
    public void limitSessions(long campusId, long userId) {
        sessionDomainService.limitSessions(campusId, userId);
    }

}
