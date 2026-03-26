package com.wsw.fitnesssystem.auth.application.service.impl;

import com.wsw.fitnesssystem.auth.application.service.LoginFailLimitService;
import com.wsw.fitnesssystem.auth.application.service.RiskControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 风控服务实现类
 *
 * <p>负责登录相关的风控策略处理，包括：
 * <ul>
 *     <li>登录前检查：账号是否被锁定、失败次数限制</li>
 *     <li>登录失败处理：记录失败次数，达到阈值锁定账号</li>
 *     <li>登录成功处理：重置失败次数并解锁账号</li>
 * </ul>
 *
 * <p>该实现封装了 LoginFailLimitService，提供统一接口给应用服务调用，
 * 使 LoginApplicationService 保持流程清晰，职责单一。
 * @author loriyuhv
 * @version 1.0 2026/3/21 13:58
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class RiskControlServiceImpl implements RiskControlService {
    /** 登录失败次数限制服务，封装具体策略和锁定逻辑 */
    private final LoginFailLimitService loginFailLimitService;

    @Override
    public void preCheck(String username) {
        loginFailLimitService.checkLock(null, username);
        loginFailLimitService.check(null, username);
    }

    @Override
    public int onFail(String username) {
        int count = loginFailLimitService.recordFail(null, username);

        if (count >= 5) {
            loginFailLimitService.lock(null, username);
            return count;
        }
        return count;
    }

    @Override
    public void onSuccess(Long campusId, String username) {
        loginFailLimitService.reset(null, username);
        loginFailLimitService.unlock(campusId, username);
    }
}
