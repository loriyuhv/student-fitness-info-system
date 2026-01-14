package com.wsw.fitnesssystem.infrastructure.security.model;

import com.wsw.fitnesssystem.domain.user.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:29
 * @since 1.0
 */
@Getter
public class LoginUser implements UserDetails {
    private final User user;

    /**
     * 当前用户的权限集合
     */
    private final Set<String> authorities;

    public LoginUser(User user, Set<String> authorities) {
        this.user = user;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities.stream()
            .map(SimpleGrantedAuthority::new)
            .toList();
    }

    /* ================== UserDetails 必须实现的方法 ================== */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    @Override
    public String toString() {
        return "LoginUser{" +
            "userId=" + user.getUserId() +
            ", username='" + user.getUsername() + '\'' +
            ", authorities=" + authorities +
            '}';
    }
}
