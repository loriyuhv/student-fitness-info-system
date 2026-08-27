package com.wsw.fitnesssystem.auth.risk.domain.port;

import com.wsw.fitnesssystem.auth.risk.domain.model.AccountRiskProfile;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.AccountIdentifier;

import java.util.Optional;

/**
 * 账号风控仓储接口 - 领域端口
 *
 * <p>领域层定义契约，基础设施层实现。</p>
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:15
 * @since 1.0
 */
public interface AccountRiskRepository {

    Optional<AccountRiskProfile> findByIdentifier(AccountIdentifier identifier);

    void save(AccountRiskProfile profile);

    void delete(AccountIdentifier identifier);

}
