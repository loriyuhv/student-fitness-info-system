package com.wsw.fitnesssystem.auth.authentication.application.port;

import com.wsw.fitnesssystem.auth.authentication.application.dto.port.RiskCheckResult;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/27 11:45
 * @since 1.0
 */
public interface RiskPort {

    /** 登录前检查 */
    void preCheck(String username);

    /** 登录失败处理 */
    RiskCheckResult onFail(String username);

    /** 登录成功处理 */
    void onSuccess(String username);

}
