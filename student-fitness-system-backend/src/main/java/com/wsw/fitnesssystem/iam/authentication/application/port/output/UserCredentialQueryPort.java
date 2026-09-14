package com.wsw.fitnesssystem.iam.authentication.application.port.output;

import com.wsw.fitnesssystem.iam.authentication.application.dto.result.UserCredentialResult;

/**
 * 用户凭证查询端口。
 *
 * <p><b>契约归属：</b>由 authentication 模块定义，user 模块实现。</p>
 * <p><b>用途：</b>登录认证、Token 刷新场景下获取用户凭证信息。</p>
 *
 * <p><b>演进：</b>本地阶段由 {@code UserCredentialQueryLocalAdapter} 实现，
 * 微服务阶段由 {@code UserCredentialQueryFeignAdapter} 实现，调用方零改动。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 15:13
 * @since 1.0
 */
public interface UserCredentialQueryPort {

    /**
     * 按登录账号查询用户凭证。
     *
     * @param username 登录账号
     * @return 凭证数据；用户不存在返回 null
     */
    UserCredentialResult findByUsername(String username);

    /**
     * 按校区 ID + 用户 ID 查询（Token 刷新场景）。
     *
     * @param campusId 校区 ID
     * @param userId   用户 ID
     * @return 凭证数据；用户不存在返回 null
     */
    UserCredentialResult findByCampusIdAndUserId(Long campusId, Long userId);

}
