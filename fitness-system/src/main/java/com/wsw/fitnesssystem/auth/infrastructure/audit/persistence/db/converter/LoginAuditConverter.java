package com.wsw.fitnesssystem.auth.infrastructure.audit.persistence.db.converter;

import com.wsw.fitnesssystem.auth.domain.audit.model.LoginAudit;
import com.wsw.fitnesssystem.auth.domain.audit.valueobject.AuditId;
import com.wsw.fitnesssystem.auth.domain.audit.valueobject.DeviceInfo;
import com.wsw.fitnesssystem.auth.domain.audit.valueobject.IpAddress;
import com.wsw.fitnesssystem.auth.domain.audit.valueobject.LogoutReason;
import com.wsw.fitnesssystem.auth.infrastructure.audit.persistence.db.entity.SysUserLogin;

/**
 * 登录审计对象转换器
 *
 * <p>职责：Entity（SysUserLogin）↔ 领域对象（LoginAudit）双向转换</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/25 12:10
 * @since 1.0
 */
public final class LoginAuditConverter {
    private LoginAuditConverter() {
    }

    /**
     * 领域对象 → Entity（保存用）
     */
    public static SysUserLogin toEntity(LoginAudit audit) {
        SysUserLogin entity = new SysUserLogin();
        entity.setUserId(audit.getUserId());
        entity.setUsername(audit.getUsername());
        entity.setLoginType(audit.isFailure() ? 0 : 1);
        entity.setFailReason(audit.isFailure() ? audit.getFailureContext().failReason() : null);
        entity.setTokenId(audit.getTokenId());
        entity.setDeviceType(audit.getDevice() != null ? audit.getDevice().deviceType() : null);
        entity.setClientInfo(audit.getDevice() != null ? audit.getDevice().userAgent() : null);
        entity.setLoginIp(audit.getIp() != null ? audit.getIp().value() : null);
        entity.setLoginTime(audit.getLoginTime());
        entity.setExpireTime(audit.getTokenSnapshot() != null ? audit.getTokenSnapshot().expireTime() : null);
        entity.setLogoutTime(audit.getLogoutTime());
        entity.setLogoutReason(audit.getLogoutReason() != null ? audit.getLogoutReason().name() : null);
        entity.setStatus(audit.isOnline() ? 1 : 0);
        return entity;
    }

    /**
     * Entity → 领域对象（查询用）
     */
    public static LoginAudit toDomain(SysUserLogin entity) {
        // 根据 Entity 状态还原领域对象
        LoginAudit audit;
        if (entity.getLoginType() == 1) {
            audit = LoginAudit.recordSuccess(
                entity.getUserId(),
                entity.getUsername(),
                entity.getTokenId(),
                entity.getExpireTime(),
                new DeviceInfo(entity.getDeviceType(), entity.getClientInfo()),
                new IpAddress(entity.getLoginIp())
            );
        } else {
            audit = LoginAudit.recordFailure(
                entity.getUsername(),
                new IpAddress(entity.getLoginIp()),
                new DeviceInfo(entity.getDeviceType(), entity.getClientInfo()),
                entity.getFailReason()
            );
        }

        Long loginId = entity.getLoginId();
        audit.setId(new AuditId(loginId));

        // 如果已下线，执行 terminate
        if (entity.getLogoutTime() != null && entity.getLogoutReason() != null) {
            audit.terminate(LogoutReason.valueOf(entity.getLogoutReason()));
        }

        return audit;
    }

}
