package com.wsw.fitnesssystem.auth.audit.infrastructure.persistence.db.converter;

import com.wsw.fitnesssystem.auth.audit.domain.model.LoginAudit;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.AuditId;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.DeviceInfo;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.IpAddress;
import com.wsw.fitnesssystem.auth.audit.domain.valueobject.LogoutReason;
import com.wsw.fitnesssystem.auth.audit.infrastructure.persistence.db.entity.SysUserLogin;

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

    private LoginAuditConverter() {}

    /**
     * 领域对象 → Entity（保存用）
     */
    public static SysUserLogin toEntity(LoginAudit audit) {
        SysUserLogin entity = new SysUserLogin();

        // 如果有 ID，设置 ID（用于更新）
        if (audit.getId() != null) {
            entity.setLoginId(audit.getId().value());
        }

        entity.setUserId(audit.getUserId());
        entity.setUsername(audit.getUsername());
        entity.setLoginType(audit.isFailure() ? 0 : 1);

        if (audit.isFailure()) {
            entity.setFailReason(
                audit.getFailureContext() != null ? audit.getFailureContext().failReason() : null
            );
        }

        entity.setTokenId(audit.getTokenId());
        entity.setDeviceType(audit.getDevice() != null ? audit.getDevice().deviceType() : null);
        entity.setClientInfo(audit.getDevice() != null ? audit.getDevice().userAgent() : null);
        entity.setLoginIp(audit.getIp() != null ? audit.getIp().value() : null);
        entity.setLoginTime(audit.getLoginTime());

        if (audit.getTokenSnapshot() != null) {
            entity.setExpireTime(audit.getTokenSnapshot().expireTime());
        }

        entity.setLogoutTime(audit.getLogoutTime());
        entity.setLogoutReason(audit.getLogoutReason() != null ? audit.getLogoutReason().name() : null);
        entity.setStatus(audit.isOnline() ? 1 : 0);

        return entity;
    }

    /**
     * Entity → 领域对象（查询用）
     */
    public static LoginAudit toDomain(SysUserLogin entity) {
        LoginAudit audit;

        // 1. 根据登录类型构建审计对象
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

        // 2. 设置 ID
        if (entity.getLoginId() != null) {
            audit.setId(new AuditId(entity.getLoginId()));
        }

        // 3. 如果已下线，直接设置状态，绕过 terminate() 的业务校验
        if (entity.getLogoutTime() != null && entity.getLogoutReason() != null) {
            try {
                LogoutReason reason = LogoutReason.valueOf(entity.getLogoutReason());
                audit.markOffline(reason, entity.getLogoutTime());
            } catch (IllegalArgumentException e) {
                // 如果枚举值不匹配，记录日志并默认使用 LOGOUT
                // 这里不能打 log，因为 converter 是静态类，但可以忽略或抛异常
                // 实际项目中建议保留一个默认值
                audit.markOffline(LogoutReason.LOGOUT, entity.getLogoutTime());
            }
        }

        return audit;
    }

}
