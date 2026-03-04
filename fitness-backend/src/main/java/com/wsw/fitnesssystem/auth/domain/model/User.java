package com.wsw.fitnesssystem.auth.domain.model;

import com.wsw.fitnesssystem.shared.common.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;

/**
 * 用户聚合根
 * 只表达「和用户本身有关」的业务规则，不关心登录流程、不关心 JWT、不关心权限。
 * 注意：getPassword()：返回的是「已加密密码」
 *
 * @author loriyuhv
 * @version 1.0 2026/1/19 14:15
 * @since 1.0
 */
public record User(Long userId, String username, String password, Integer status) {
    /**
     * 领域规则：
     * 当前用户是否允许登录系统
     */
    public void assertCanLogin() {
        if (this.status == null || this.status == 0) {
            throw new BizException(ResultCode.ACCOUNT_DISABLED);
        }
    }
}
