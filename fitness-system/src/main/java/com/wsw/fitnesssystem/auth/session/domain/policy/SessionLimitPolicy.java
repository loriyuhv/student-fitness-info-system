package com.wsw.fitnesssystem.auth.session.domain.policy;

/**
 * 会话数量限制策略（Domain 层定义）
 *
 * <p>定义：系统允许单个用户同时在线多少个设备。
 * 具体值由基础设施层（配置文件、数据库、动态开关）提供。
 *
 * @author loriyuhv
 * @version 1.0 2026/8/27 07:10
 * @since 1.0
 */
public interface SessionLimitPolicy {

    /**
     * 获取允许的最大在线会话数
     * @return 最大会话数，必须 >= 1
     */
    int getMaxSessions();

}
