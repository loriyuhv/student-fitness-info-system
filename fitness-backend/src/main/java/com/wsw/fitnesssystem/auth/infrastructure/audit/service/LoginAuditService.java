package com.wsw.fitnesssystem.auth.infrastructure.audit.service;

import com.wsw.fitnesssystem.auth.application.authentication.command.LoginCommand;
import com.wsw.fitnesssystem.auth.domain.model.TokenPair;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.entity.SysUserLogin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录审计门面
 * AuthService 只和它打交道
 *
 * @author loriyuhv
 * @version 1.0 2026/1/11 16:50
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class LoginAuditService {
    private final AuditAsyncService asyncService;

    /**
     * 登录成功
     */
    public void loginSuccess(
        Long userId,
        LoginCommand command,
        TokenPair tokenPair
    ) {
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(tokenPair.getExpire() / 1000);
        SysUserLogin login = new SysUserLogin();
        login.setUserId(userId);
        login.setUsername(command.getUsername());
        login.setLoginType(1);
        login.setTokenId(tokenPair.getAccessTokenId());
        login.setLoginIp(command.getIp());
        login.setDeviceType(command.getDeviceType());
        login.setClientInfo(command.getUserAgent());
        login.setExpireTime(expireTime);
        login.setStatus(1);

        asyncService.recordLogin(login);
    }

    /**
     * 登录失败
     */
    public void loginFail(
        String username,
        String ip,
        String deviceType,
        String userAgent,
        String failReason,
        int failCount,
        boolean locked
    ) {
        SysUserLogin login = new SysUserLogin();
        login.setUsername(username);
        login.setLoginIp(ip);
        login.setDeviceType(deviceType);
        login.setClientInfo(userAgent);

        login.setLoginType(0);              // 失败
        login.setFailReason(failReason);
        login.setFailCount(failCount);
        login.setLocked(locked ? 1 : 0);

        login.setLoginTime(LocalDateTime.now());
        login.setStatus(0);

        asyncService.recordLogin(login);
    }


    /**
     * 正常退出
     */
    public void logout(Long userId, String tokenId) {
        asyncService.recordLogout(userId, tokenId, "LOGOUT");
    }

    /** 被踢下线 */
    public void kick(Long userId, String tokenId) {
        asyncService.recordLogout(userId, tokenId, "KICK");
    }

    /** Token 过期 */
    public void expire(Long userId, String tokenId) {
        asyncService.recordLogout(userId, tokenId, "EXPIRE");
    }
}
