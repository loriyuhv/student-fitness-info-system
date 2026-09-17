package com.wsw.fitnesssystem.shared.domain.vb;

import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;

/**
 * 操作者身份标识 - 共享值对象
 * <p>跨模块上下文共享，作为用户身份的最小化表达。</p>
 * <p>创建时必须提供完整字段，禁止传 null（除了登录前尚未识别 userId 的场景）。</p>
 * <p><b>campusId 语义：</b></p>
 * <ul>
 *   <li>{@code 0}：系统级（超级管理员），跨校区操作</li>
 *   <li>{@code >0}：具体校区，仅本校区可见</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/25 21:56
 * @since 1.0
 */
public record Operator(
    long campusId, long userId, String username, int userType) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 完整构造（登录成功后使用）
     *
     */
    public Operator {
        if (campusId < 0) {
            throw new IllegalArgumentException("campusId must be non-negative (0 = system-level)");
        }
        if (userId <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (userType < 0 || userType > 2) {
            throw new IllegalArgumentException("userType must be in [0, 2]");
        }
    }

}
