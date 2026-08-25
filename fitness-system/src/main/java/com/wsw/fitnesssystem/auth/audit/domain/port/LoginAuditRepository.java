package com.wsw.fitnesssystem.auth.audit.domain.port;

import com.wsw.fitnesssystem.auth.audit.domain.model.LoginAudit;

import java.util.Optional;

/**
 * 登录审计仓储接口
 *
 * @author loriyuhv
 * @version 1.0 2026/8/25 11:49
 * @since 1.0
 */
public interface LoginAuditRepository {

    /**
     * 保存登录审计日志
     * @param audit 登录审计日志
     */
    void save(LoginAudit audit);

    /**
     * 更新登录审计日志
     * @param audit 登录审计日志
     */
    void update(LoginAudit audit);

    /**
     * 通过TokenID查找审计日志
     * @param tokenId Token ID
     * @return 审计日志
     */
    Optional<LoginAudit> findByTokenId(String tokenId);

}
