package com.wsw.fitnesssystem.iam.authentication.application.port.output;

import com.wsw.fitnesssystem.iam.authentication.application.port.output.dto.RiskCheckResult;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/27 11:45
 * @since 1.0
 */
public interface RiskPort {

    /** 登录前检查 */
    void checkAccess(String username);

    /** 登录失败处理 */
    RiskCheckResult recordFailure(String username);

    /** 登录成功处理 */
    void resetState(String username);

}
