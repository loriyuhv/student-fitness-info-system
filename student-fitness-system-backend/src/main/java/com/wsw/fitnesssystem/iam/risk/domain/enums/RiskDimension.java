package com.wsw.fitnesssystem.iam.risk.domain.enums;

/**
 * 风控维度 - 枚举
 *
 * <p><b>职责：</b>标识风控主体所属的维度。维度决定了：
 * <ul>
 *   <li>Redis Key 的命名空间（fail/lock 前缀后的一段）</li>
 *   <li>使用哪一套策略参数（阈值、锁定时长、统计窗口）</li>
 *   <li>业务语义（账号锁定 / IP 限流 / 设备封禁 / 行为拦截）</li>
 * </ul></p>
 *
 * <p><b>扩展方式：</b>新增维度只需：
 * <ol>
 *   <li>在此枚举中增加一个常量</li>
 *   <li>在 {@code application.yaml} 的 {@code iam.risk.policies} 下增加对应配置</li>
 * </ol>
 * 无需修改仓储、Key 工厂、聚合根等骨架代码。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/26 07:47
 * @since 1.0
 */
public enum RiskDimension {

    /**
     * 账号维度：登录失败次数触发的账号锁定
     */
    USER,

    /**
     * IP 维度：同一 IP 的异常行为限制（预留）
     */
    IP

}
