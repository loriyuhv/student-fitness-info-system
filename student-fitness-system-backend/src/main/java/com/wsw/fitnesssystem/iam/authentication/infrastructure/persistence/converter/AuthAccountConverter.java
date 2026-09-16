package com.wsw.fitnesssystem.iam.authentication.infrastructure.persistence.converter;

import com.wsw.fitnesssystem.iam.authentication.domain.model.AuthAccount;
import com.wsw.fitnesssystem.iam.authentication.domain.vb.AccountStatus;
import com.wsw.fitnesssystem.iam.authentication.domain.vb.UserSource;
import com.wsw.fitnesssystem.iam.authentication.domain.vb.UserType;
import com.wsw.fitnesssystem.iam.authentication.infrastructure.persistence.entity.SysUserPo;

/**
 * AuthAccount 领域模型 ↔ SysUserPo 转换器
 *
 * @author loriyuhv
 * @version 1.0 2026/9/16 15:00
 * @since 1.0
 */
public final class AuthAccountConverter {

    private AuthAccountConverter() {}

    public static AuthAccount toDomain(SysUserPo po) {
        if (po == null) return null;
        boolean deleted = po.getDeleted() == 1;
        return AuthAccount.reconstitute(
            po.getUserId(),
            po.getCampusId(),
            po.getUsername(),
            po.getPassword(),
            UserType.of(po.getUserType()),
            UserSource.of(po.getSource()),
            AccountStatus.of(po.getStatus()),
            deleted,
            po.getCreateBy(),
            po.getUpdateBy()
        );
    }

    public static SysUserPo toPo(AuthAccount account) {
        if (account == null) return null;
        SysUserPo po = new SysUserPo();
        po.setUserId(account.getUserId());
        po.setCampusId(account.getCampusId());
        po.setUsername(account.getUsername());
        po.setPassword(account.getPasswordHash());
        po.setUserType(account.getUserType().getCode());
        po.setSource(account.getSource().getCode());
        po.setStatus(account.getStatus().getCode());
        po.setDeleted(account.isDeleted() ? 1 : 0);
        po.setCreateBy(account.getCreateBy());
        po.setUpdateBy(account.getUpdateBy());
        return po;
    }

}
