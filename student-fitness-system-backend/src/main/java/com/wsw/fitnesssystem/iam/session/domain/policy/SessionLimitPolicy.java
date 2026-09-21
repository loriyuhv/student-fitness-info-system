package com.wsw.fitnesssystem.iam.session.domain.policy;

/**
 * 会话数量限制策略（领域策略接口）
 *
 * <p><b>职责：</b>定义"单个用户允许同时在线多少个设备"这一业务规则，
 * 具体阈值由外部配置 / 数据库 / 动态开关提供。</p>
 *
 * <p><b>设计意图：</b>把"业务规则"与"规则来源"解耦，领域服务只依赖本接口，
 * 不关心阈值从配置文件、数据库还是配置中心读取。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/27 07:10
 * @since 1.0
 */
public interface SessionLimitPolicy {

    /**
     * 获取允许的最大在线会话数
     *
     * @return 最大会话数，必须 >= 1
     */
    int getMaxSessions();

}
