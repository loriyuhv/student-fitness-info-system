package com.wsw.fitnesssystem.auth.infrastructure.audit;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.db.entity.SysUserLogin;
import com.wsw.fitnesssystem.auth.infrastructure.persistence.db.mapper.SysUserLoginMapper;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
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
     *
     * @param login 记录对象
     */
    @Async
    public void recordLogin(SysUserLogin login) {
        try {
            loginMapper.insert(login);
            log.info("[登录审计记录成功] userId={}, tokenId={}, loginIp={}",
                login.getUserId(), login.getTokenId(), login.getLoginIp());
        } catch (Exception e) {
            log.error("[登录审计记录失败] userId={}, tokenId={}",
                login.getUserId(), login.getTokenId(), e);
        }
    }

    /**
     * 场景：用户登出、管理员强制下线用户、用户修改密码全局失效
     *
     * @param operator 操作对象
     * @param tokenId AccessToken标识
     * @param logoutReason 失败原因
     */
    @Async
    public void recordLogout(Operator operator, String tokenId, String logoutReason) {
        try {
            int affectedRows = loginMapper.update(
                null,
                Wrappers
                    .<SysUserLogin>lambdaUpdate()
                    .eq(SysUserLogin::getUserId, operator.userId())
                    .eq(SysUserLogin::getTokenId, tokenId)
                    .eq(SysUserLogin::getStatus, 1)   // 只更新“在线”的
                    .set(SysUserLogin::getStatus, 0)
                    .set(SysUserLogin::getLogoutTime, LocalDateTime.now())
                    .set(SysUserLogin::getLogoutReason, logoutReason)
            );

            log.info("[登出审计记录成功] userId={}, tokenId={}, reason={}, affectedRows={}",
                operator.userId(), tokenId, logoutReason, affectedRows);

        } catch (Exception e) {
            log.error("[登出审计记录失败] userId={}, tokenId={}",
                operator.userId(), tokenId, e);
        }
    }

}
