package com.wsw.fitnesssystem.auth.risk.application;

import com.wsw.fitnesssystem.auth.risk.application.impl.RiskControlAppService;
import com.wsw.fitnesssystem.auth.risk.domain.valueobject.RiskFailResult;

/**
 * 风控应用服务接口
 *
 * <p>对外暴露的契约。由 {@link RiskControlAppService} 实现。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/3/21 13:58
 * @since 1.0
 */
public interface RiskControlService {

    /**
     * 登录前检查
     * @param username 用户登录名
     */
    void preCheck(String username);

    /**
     * 登录失败处理，返回风控结果
     * @param username 用户登录名
     * @return 失败结果
     */
    RiskFailResult onFail(String username);

    /**
     * 登录成功处理（重置风控状态）
     * @param username 用户登录名
     */
    void onSuccess(String username);

    /**
     * 管理员手动解封账号
     * @param username 用户登录名
     */
    void unlockAccount(String username);

}
