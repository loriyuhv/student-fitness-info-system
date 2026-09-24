package com.wsw.fitnesssystem.iam.authorization.application.port.output;

import com.wsw.fitnesssystem.iam.authorization.application.dto.result.UserAuthorization;

/**
 * <p>权限缓存端口（六边形架构的 output port）</p>
 *
 * <p>
 *     应用层通过此接口访问缓存，不感知底层是 Redis、Caffeine 还是别的实现。
 *     本接口只暴露业务语义，不暴露任何缓存技术细节。
 * </p>
 *
 * <p>
 *     稳定性承诺：
 *     <li>本接口一旦发布，实现变更（如从 Redis 换到本地缓存）不应影响调用方</li>
 *     <li>方法签名只描述业务意图，不出现 Redis / Key / TTL 等技术词汇</li>
 *     </p>
 *
 * @author loriyuhv
  * @version 1.0 2026/1/16 14:10
 * @since 1.0
 */
public interface AuthorizationCachePort {

    /**
     * 缓存用户授权信息
     *
     * @param userId        用户 ID
     * @param campusId      校区 ID
     * @param authorization 授权对象
     */
    void cache(long userId, long campusId, UserAuthorization authorization);

    /**
     * 获取用户授权信息
     *
     * @param userId   用户 ID
     * @param campusId 校区 ID
     * @return 授权对象；不存在时返回 null
     */
    UserAuthorization get(long userId, long campusId);

    /**
     * 清除用户授权缓存
     *
     * @param userId   用户 ID
     * @param campusId 校区 ID
     */
    void evict(long userId, long campusId);

}
