package com.wsw.fitnesssystem.auth.audit.application.service.impl;

import com.wsw.fitnesssystem.auth.audit.application.service.AuditAppService;
import com.wsw.fitnesssystem.auth.audit.domain.model.LoginAudit;
import com.wsw.fitnesssystem.auth.audit.domain.port.LoginAuditRepository;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.DeviceInfo;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.IpAddress;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.LogoutReason;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 审计应用服务
 *
 * <p>职责边界（DDD Lite）：
 * <ul>
 *     <li>接收参数，构建领域对象</li>
 *     <li>调用仓储保存</li>
 *     <li>异步执行（应用层决定技术细节）</li>
 * </ul>
 * @author loriyuhv
 * @version 1.0 2026/8/25 12:02
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditAppServiceImpl implements AuditAppService {

    private final LoginAuditRepository auditRepository;

    /** 记录登录成功（异步） */
    public void recordLoginSuccess(
        Long userId, String username, String tokenId,
        LocalDateTime expireTime, String deviceType, String userAgent, String ip
    ) {
        try {
            LoginAudit audit = LoginAudit.recordSuccess(
                userId, username, tokenId, expireTime,
                new DeviceInfo(deviceType, userAgent), new IpAddress(ip)
            );

            auditRepository.save(audit);

            log.debug("登录成功审计已记录: userId={}, tokenId={}", userId, tokenId);
        } catch (BizException e) {
            log.error("记录登录成功审计业务异常: userId={}，message={}", userId, e.getMessage());
        } catch (Exception e) {
            log.error("记录登录成功审计失败: userId={}", userId, e);
        }
    }

    /** 记录登录失败（异步）*/
    public void recordLoginFailure(
        String username, String ip, String deviceType, String userAgent, String failReason
    ) {
        try {
            LoginAudit audit = LoginAudit.recordFailure(
                username, new IpAddress(ip), new DeviceInfo(deviceType, userAgent), failReason
            );
            auditRepository.save(audit);
            log.debug("登录失败审计已记录: username={}, reason={}", username, failReason);
        } catch (BizException e) {
            log.warn("记录登录失败审计业务异常: username={}, message={}", username, e.getMessage());
        } catch (Exception e) {
            log.error("记录登录失败审计失败: username={}", username, e);
        }
    }

    /**
     * 标记会话终止（异步）
     *
     * @param tokenId TokenID
     * @param reason 登出原因
     */
    public void terminateSession(String tokenId, LogoutReason reason) {
        try {
            LoginAudit audit = auditRepository.findByTokenId(tokenId).orElseThrow(
                () -> new BizException(ResultCode.SESSION_NOT_FOUND, "Token not found: " + tokenId)
            );
            audit.terminate(reason);
            auditRepository.update(audit);
            log.debug("会话终止已记录: tokenId={}, reason={}", tokenId, reason);
        } catch (BizException e) {
            // 业务异常单独记录 WARN，不吞掉
            log.warn("标记会话终止业务异常: tokenId={}, reason={}, message={}",
                tokenId, reason, e.getMessage());
        } catch (Exception e) {
            log.error("标记会话终止失败: tokenId={}, reason={}", tokenId, reason, e);
        }
    }

}
