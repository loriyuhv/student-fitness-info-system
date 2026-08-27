package com.wsw.fitnesssystem.auth.risk.domain.port;

import com.wsw.fitnesssystem.auth.risk.domain.model.AccountRiskProfile;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.AccountIdentifier;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.RiskFailResult;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.RiskPolicy;

import java.util.Optional;

/**
 * 账号风控仓储接口 - 领域端口
 *
 * <p>领域层定义契约，基础设施层实现。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/10 21:15
 * @since 1.0
 */
public interface AccountRiskRepository {

    /**
     * 查询风控状态（用于登录前检查、管理员查看）
     * @param identifier 账户标识 用户账号等
     * @return 账号风控画像
     */
    Optional<AccountRiskProfile> findByIdentifier(AccountIdentifier identifier);

    /**
     * <p>原子操作：失败计数 +1，并判断是否达到锁定阈值</p>
     * <p>这是高并发路径的唯一入口，全程在 Redis 服务端执行</p>
     *
     * @param identifier 账号标识
     * @param policy     风控策略
     * @return 风控失败结果（包含失败次数、是否锁定、剩余尝试次数）
     */
    RiskFailResult incrementFailAndGet(AccountIdentifier identifier, RiskPolicy policy);

    /**
     * 删除风控记录（登录成功、管理员解封时调用）
     *
     * @param identifier 账号标识
     */
    void delete(AccountIdentifier identifier);

}
