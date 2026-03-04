package com.wsw.fitnesssystem.identity.infrastructure.security.model;

import com.wsw.fitnesssystem.identity.domain.model.AuthUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Spring Security 用户适配模型
 * 只用于：
 *  - 注入 SecurityContext
 *  - 提供权限信息
 * 不承载：
 *  - 业务规则
 *  - 登录逻辑
 *  - 禁用判断
 *
 *  @author loriyuhv
 *  @version 1.0 2026/1/14 12:29
 *  @since 1.0
 */
@Getter
public class SecurityUser implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    private SecurityUser(
        Long userId,
        String username,
        String password,
        Collection<? extends GrantedAuthority> authorities
    ) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * 从领域用户转换为 Security 用户
     */
    public static SecurityUser from(AuthUser authUser) {
        Objects.requireNonNull(authUser, "authUser must not be null");

        return new SecurityUser(
            authUser.getUserId(),
            authUser.getUsername(),
            authUser.getPassword(),
            loadAuthorities(authUser)
        );
    }

    /**
     * 权限加载逻辑
     * ⚠️ 注意：
     * - 这里是 Security 关注点
     * - Domain 永远不感知权限
     * 实际项目中可以：
     * - 查 Redis
     * - 查 DB
     * - 查缓存
     */
    private static Collection<? extends GrantedAuthority>
    loadAuthorities(AuthUser user) {

        // 示例：最小实现
        // 真实项目中一般从 DB / Redis 查询角色和权限
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_USER")
        );
    }

    // =========================
    // UserDetails 接口实现
    // =========================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 下面 4 个状态方法：
     * <p>统一返回 true 的原因：</p>
     * <ul>
     *     <li>业务状态（禁用 / 锁定）已经在“登录用例”中校验过</li>
     *     <li>Security 这里只负责“已登录用户的请求鉴权”</li>
     * </ul>
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
