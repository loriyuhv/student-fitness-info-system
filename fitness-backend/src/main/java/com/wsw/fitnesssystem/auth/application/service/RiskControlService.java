package com.wsw.fitnesssystem.auth.application.service;

/**
 * 风控服务接口
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
public interface RiskControlService {
    /**
     * 登录前检查
     *
     * <p>主要检查：
     * <ul>
     *     <li>账号是否被锁定</li>
     *     <li>账号失败次数是否超过限制</li>
     * </ul>
     *
     * @param username 用户登录名
     */
    void preCheck(String username);

    /**
     * 登录失败处理
     *
     * <p>记录失败次数，并在失败次数达到阈值时锁定账号。
     *
     * @param username 用户登录名
     * @return 失败次数
     */
    int onFail(String username);

    /**
     * 登录成功处理
     *
     * <p>登录成功后：
     * <ul>
     *     <li>重置账号失败次数</li>
     *     <li>解锁账号（如果之前被锁定）</li>
     * </ul>
     *
     * @param campusId 校区ID，用于多校区系统解锁
     * @param username 用户登录名
     */
    void onSuccess(Long campusId, String username);
}
