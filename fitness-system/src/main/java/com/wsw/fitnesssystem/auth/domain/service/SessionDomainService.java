package com.wsw.fitnesssystem.auth.domain.service;

/**
 * 登录会话领域服务（Domain Service）
 *
 * <p>
 * 这是领域层的服务接口，用于处理与用户会话相关的业务规则。
 * 注意：Domain Service 只关注业务逻辑，不依赖具体存储或技术实现。
 * </p>
 *
 * <p>主要职责：</p>
 * <ul>
 *     <li>处理多端登录策略（最大登录设备数限制）</li>
 *     <li>根据策略决定哪些会话需要被踢掉</li>
 *     <li>配合 SessionRepository 完成会话管理</li>
 * </ul>
 *
 * <p>面试亮点：</p>
 * <ul>
 *     <li>Domain Service 只处理业务规则，Infrastructure 层负责具体实现（如 Redis 操作）</li>
 *     <li>符合 DDD 原则，业务逻辑与技术实现解耦</li>
 *     <li>支持多端登录限制、单点登录、踢掉最早登录设备等功能</li>
 * </ul>
 * @author loriyuhv
 * @version 1.0 2026/3/21 10:05
 * @since 1.0
 */
public interface SessionDomainService {
    /**
     * 限制用户最大登录设备数
     *
     * @param campusId 校区ID（多租户隔离）
     * @param userId 用户ID
     * @param maxSessions 最大允许同时在线的会话数
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
     */
    void limitSessions(Long campusId, Long userId, int maxSessions);
}
