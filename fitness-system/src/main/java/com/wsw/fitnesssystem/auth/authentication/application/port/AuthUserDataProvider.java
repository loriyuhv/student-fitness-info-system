package com.wsw.fitnesssystem.auth.authentication.application.port;

import com.wsw.fitnesssystem.auth.authentication.application.dto.port.AuthUserCredential;

/**
 * 用户认证数据提供者端口（由 auth.authentication 模块定义，user 模块实现）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>定义 auth 模块对用户数据的依赖契约</li>
 *   <li>只暴露认证所需的最小数据集（密码哈希、锁定状态等）</li>
 *   <li>不依赖任何具体实现（本地 JVM 调用 / 远程 RPC 均透明）</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 14:12
 * @since 1.0
 */
public interface AuthUserDataProvider {

    /**
     * 根据用户名获取认证数据
     *
     * @param username 用户名（唯一索引）
     * @return 认证数据对象，若用户不存在则返回 null
     */
    AuthUserCredential getAuthDataByUsername(String username);

    /**
     * 根据CampusId + UserId获取认证数据
     *
     * @param campusId 校园ID
     * @param userId 用户ID
     * @return 认证数据对象，若用户不存在则返回 null
     */
    AuthUserCredential getAuthDataByCampusIdAndUserId(long campusId, long userId);
    
}
