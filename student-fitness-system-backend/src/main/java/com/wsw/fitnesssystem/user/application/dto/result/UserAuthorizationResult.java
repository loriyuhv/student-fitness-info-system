package com.wsw.fitnesssystem.user.application.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

/**
 * 用户授权查询结果。
 *
 * <p><b>契约约束：</b></p>
 * <ul>
 *   <li>只包含角色编码与权限编码，不含权限详情（名称、描述等）</li>
 *   <li>集合永不为 null，无授权时返回空集合</li>
 *   <li>跨模块传输，不添加任何 Web 序列化注解</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 15:37
 * @since 1.0
 */
@Getter
@Builder
public class UserAuthorizationResult {

    /** 角色编码集合（如 ADMIN、TEACHER） */
    private Set<String> roles;

    /** 权限编码集合（如 system:user:view） */
    private Set<String> permissions;

    /**
     * 空结果工厂方法，避免调用方判空。
     */
    public static UserAuthorizationResult empty() {
        return UserAuthorizationResult.builder()
            .roles(Set.of())
            .permissions(Set.of())
            .build();
    }

}
