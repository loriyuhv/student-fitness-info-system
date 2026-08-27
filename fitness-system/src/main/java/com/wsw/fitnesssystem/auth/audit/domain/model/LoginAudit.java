package com.wsw.fitnesssystem.auth.audit.domain.model;

import com.wsw.fitnesssystem.auth.audit.domain.valueobject.*;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 登录审计记录 - 聚合根
 *
 * <p>职责边界：
 * <ul>
 *     <li>记录用户登录行为（成功/失败）</li>
 *     <li>追踪会话生命周期（在线 → 下线）</li>
 * </ul>
 *
 * <p>不变量：
 * <ol>
 *     <li>登录成功时，tokenSnapshot 不能为空</li>
 *     <li>登录失败时，failureContext 不能为空</li>
 *     <li>已下线的记录不能再下线</li>
 * </ol>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/25 11:43
 * @since 1.0
 */
@Getter
public class LoginAudit {

    @Setter
    private AuditId id;
    private Long userId;
    private String username;
    private LoginResult result;
    private FailureContext failureContext;
    private TokenSnapshot tokenSnapshot;
    private DeviceInfo device;
    private IpAddress ip;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private LogoutReason logoutReason;
    private OnlineStatus status;

    // ========== 工厂方法：记录成功登录 ==========

    public static LoginAudit recordSuccess(
        Long userId,
        String username,
        String tokenId,
        LocalDateTime expireTime,
        DeviceInfo device,
        IpAddress ip
    ) {
        if (tokenId == null || tokenId.isBlank()) {
            throw new BizException(ResultCode.TOKEN_INVALID, "登录成功时 tokenId 不能为空");
        }

        LoginAudit audit = new LoginAudit();
        audit.userId = userId;
        audit.username = username;
        audit.result = LoginResult.SUCCESS;
        audit.tokenSnapshot = new TokenSnapshot(tokenId, expireTime);
        audit.device = device;
        audit.ip = ip;
        audit.loginTime = LocalDateTime.now();
        audit.status = OnlineStatus.ONLINE;
        return audit;
    }

    // ========== 工厂方法：记录失败登录 ==========

    public static LoginAudit recordFailure(
        String username,
        IpAddress ip,
        DeviceInfo device,
        String failReason
    ) {
        LoginAudit audit = new LoginAudit();
        audit.username = username;
        audit.result = LoginResult.FAILURE;
        audit.failureContext = new FailureContext(failReason);
        audit.device = device;
        audit.ip = ip;
        audit.loginTime = LocalDateTime.now();
        audit.status = OnlineStatus.NEVER_ONLINE;
        return audit;
    }

    // ========== 领域行为 ==========

    /**
     * 标记会话终止
     *
     * @throws BizException 已下线时抛出
     */
    public void terminate(LogoutReason reason) {
        if (this.status == OnlineStatus.OFFLINE) {
            throw new BizException(ResultCode.LOGOUT_FAILED, "会话已下线，不能重复终止");
        }
        if (this.status == OnlineStatus.NEVER_ONLINE) {
            throw new BizException(ResultCode.AUTH_USER_NOT_LOGIN, "登录失败记录不存在会话终止");
        }
        this.logoutTime = LocalDateTime.now();
        this.logoutReason = reason;
        this.status = OnlineStatus.OFFLINE;
    }

    public boolean isOnline() {
        return this.status == OnlineStatus.ONLINE;
    }

    public boolean isFailure() {
        return this.result == LoginResult.FAILURE;
    }

    public boolean hasToken() {
        return this.tokenSnapshot != null;
    }

    public String getTokenId() {
        return this.tokenSnapshot != null ? this.tokenSnapshot.tokenId() : null;
    }

}
