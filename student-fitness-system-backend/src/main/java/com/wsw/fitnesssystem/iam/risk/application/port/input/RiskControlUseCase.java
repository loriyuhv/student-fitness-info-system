package com.wsw.fitnesssystem.iam.risk.application.port.input;

import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskFailResult;
import com.wsw.fitnesssystem.iam.risk.domain.vb.RiskSubject;

/**
 * 风控应用服务接口 - 入站端口
 *
 * <p><b>对外契约：</b>所有方法以 {@link RiskSubject} 为主体，
 * 不绑定具体维度。调用方通过 {@code RiskSubject.user(...)} 等
 * 便捷工厂构造主体。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/3/21 13:58
 * @since 1.0
 */
public interface RiskControlUseCase {

    /**
     * 访问前检查（登录 / 敏感操作前）。
     *
     * @param subject 风控主体
     */
    void preCheck(RiskSubject subject);

    /**
     * 访问失败处理，返回风控结果。
     *
     * @param subject 风控主体
     * @return 失败结果
     */
    RiskFailResult onFail(RiskSubject subject);

    /**
     * 访问成功处理（重置风控状态）。
     *
     * @param subject 风控主体
     */
    void onSuccess(RiskSubject subject);

    /**
     * 管理员手动解封。
     *
     * @param subject 风控主体
     */
    void unlock(RiskSubject subject);

}
