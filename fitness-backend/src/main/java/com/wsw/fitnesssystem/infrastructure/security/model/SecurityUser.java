package com.wsw.fitnesssystem.infrastructure.security.model;

import com.wsw.fitnesssystem.domain.auth.model.AuthUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:29
 * @since 1.0
 */
@Getter
public class SecurityUser implements UserDetails {
    /**
     * 用户信息
     * */
    private final AuthUser authUser;

    /**
     * 当前用户的权限集合
     */
    private final Set<GrantedAuthority> authorities;

    public SecurityUser(AuthUser authUser) {
        this.authUser = authUser;
        this.authorities =
            authUser.allAuthorities().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    public Long getUserId() {
        return authUser.getUserId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /* ================== UserDetails 必须实现的方法 ================== */

    @Override
    public String getPassword() {
        return authUser.getPassword();
    }

    @Override
    public String getUsername() {
        return authUser.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return authUser.isEnabled();
    }

    @Override
    public String toString() {
        return "SecurityUser{" +
            "userId=" + authUser.getUserId() +
            ", username='" + authUser.getUsername() + '\'' +
            ", authorities=" + authorities +
            '}';
    }
}
