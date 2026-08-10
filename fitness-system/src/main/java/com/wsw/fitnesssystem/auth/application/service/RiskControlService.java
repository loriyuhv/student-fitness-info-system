package com.wsw.fitnesssystem.auth.application.service;

import com.wsw.fitnesssystem.auth.application.risk.RiskControlAppService;
import com.wsw.fitnesssystem.auth.domain.risk.valueobject.RiskFailResult;

/**
 * 风控应用服务接口
 *
 * <p>对外暴露的契约。由 {@link RiskControlAppService} 实现。</p>
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
     * @return 失败次数
     */
    RiskFailResult onFail(String username);

    /**
     * 登录成功处理
     * @param username 用户登录名
     */
    void onSuccess(String username);
}
