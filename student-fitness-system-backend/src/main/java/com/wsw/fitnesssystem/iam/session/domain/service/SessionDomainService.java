package com.wsw.fitnesssystem.iam.session.domain.service;

/**
 * 登录会话领域服务
 *
 * <p><b>职责：</b>封装会话相关的业务规则，与具体存储技术解耦。
 * 只依赖 {@code SessionRepository} 和 {@code SessionLimitPolicy} 两个抽象。</p>
 *
 * <p><b>边界：</b>
 * <ul>
 *   <li>不依赖 Spring / Redis / JWT</li>
 *   <li>不处理 HTTP、协议转换、审计事件等应用层职责</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/3/21 10:05
 * @since 1.0
 */
public interface SessionDomainService {

    /**
     * 限制用户最大在线设备数
     *
     * <p>业务说明：</p>
     * <ul>
     *     <li>如果当前在线会话数超过 maxSessions，则根据策略踢掉最早登录的会话</li>
     *     <li>仅处理业务规则，不直接操作 Redis 或 JWT</li>
     *     <li>配合 SessionRepository 获取在线会话并执行删除操作</li>
     * </ul>
     *
     * <p>概括：</p>
     * <ul>
     *     <li>体现领域服务的作用：专注业务规则而非技术实现</li>
     *     <li>体现多端登录控制策略：避免账号被无限制多端使用</li>
     * </ul>
     *
     */
    void limitSessions(long campusId, long userId);

}
