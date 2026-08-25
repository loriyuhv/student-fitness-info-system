package com.wsw.fitnesssystem.auth.shared.vo;

import java.io.Serial;
import java.io.Serializable;

/**
 * 操作者身份标识 - 共享值对象
 * <p>跨四个限界上下文（authentication/authorization/risk/audit）共享，
 * 作为用户身份的最小化表达。</p>
 * <p>创建时必须提供完整字段，禁止传 null（除了登录前尚未识别 userId 的场景）。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/25 21:56
 * @since 1.0
 */
public record Operator(long campusId, long userId, String username, int userType)
    implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 完整构造（登录成功后使用）
     *
     */

    public Operator {
        if (campusId <= 0) {
            throw new IllegalArgumentException("campusId must be positive");
        }
        if (userId <= 0) {
            throw new IllegalArgumentException("campusId must be positive");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (userType < 0 || userType > 2) {
            throw new IllegalArgumentException("userType must be non-negative");
        }
    }

}
