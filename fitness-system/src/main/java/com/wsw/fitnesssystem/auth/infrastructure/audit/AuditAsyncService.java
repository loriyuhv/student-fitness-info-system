package com.wsw.fitnesssystem.auth.infrastructure.audit;

import com.wsw.fitnesssystem.auth.infrastructure.persistence.db.entity.SysUserLogin;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.db.mapper.SysUserLoginMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 审计异步执行器
 * 注意：
 * - 所有方法都是真·异步
 * - 失败不会影响主流程
 *
 * @author loriyuhv
 * @version 1.0 2026/1/11 16:50
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditAsyncService {
    private final SysUserLoginMapper loginMapper;

    /**
     * 登录成功审计（异步）
     */
    @Async
    public void recordLogin(SysUserLogin login) {
        try {
            loginMapper.insert(login);
        } catch (Exception e) {
            log.error("记录登录审计失败 userId={}, tokenId={}",
                login.getUserId(), login.getTokenId(), e);
        }
    }

    /**
     * 退出 / 被踢 / 失效（异步）
     */
    @Async
    public void recordLogout(Long userId, String tokenId, String logoutReason) {
        try {
            loginMapper.update(
                null,
                com.baomidou.mybatisplus.core.toolkit.Wrappers
                    .<SysUserLogin>lambdaUpdate()
                    .eq(SysUserLogin::getUserId, userId)
                    .eq(SysUserLogin::getTokenId, tokenId)
                    .eq(SysUserLogin::getStatus, 1)   // 只更新“在线”的
                    .set(SysUserLogin::getStatus, 0)
                    .set(SysUserLogin::getLogoutTime, LocalDateTime.now())
                    .set(SysUserLogin::getLogoutReason, logoutReason)
            );
        } catch (Exception e) {
            log.error("记录登出审计失败 userId={}, tokenId={}",
                userId, tokenId, e);
        }
    }
}
